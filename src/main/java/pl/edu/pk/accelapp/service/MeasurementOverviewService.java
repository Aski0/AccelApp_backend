package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.dto.OverviewBlockDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.MeasurementRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementOverviewService {

    private final MeasurementRepository measurementRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Overview z podziałem na bloki po blockSize próbek.
     * Dla każdego bloku liczymy statystyki (min, max, mean) dla grup kanałów:
     * - kanały grupujemy po 3: (1-3), (4-6), (7-8) itd.
     * - ostatnia grupa może mieć 1–2 kanały
     */
    public List<OverviewBlockDto> getOverview(Long fileId,
                                              Authentication authentication,
                                              int blockSize) {

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User currentUser = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: file does not belong to user");
        }

        List<Measurement> measurements = measurementRepository.findByUploadedFileId(fileId);
        if (measurements.isEmpty() || blockSize <= 0) {
            return Collections.emptyList();
        }

        int totalSamples = measurements.size();
        int nBlocks = totalSamples / blockSize;
        if (nBlocks == 0) {
            return Collections.emptyList();
        }

        int channelCount = detectChannelCount(measurements);
        if (channelCount == 0) {
            return Collections.emptyList();
        }

        List<int[]> channelGroups = buildChannelGroups(channelCount);

        List<OverviewBlockDto> blocks = new ArrayList<>();

        for (int b = 0; b < nBlocks; b++) {
            int start = b * blockSize;
            int end = start + blockSize;

            // czas bloku – bierzemy z pierwszej próbki w bloku
            double timeSec = safe(measurements.get(start).getTime());

            // przygotowanie struktur statystyk dla grup
            int gCount = channelGroups.size();
            double[] mins = new double[gCount];
            double[] maxs = new double[gCount];
            double[] sums = new double[gCount];

            for (int g = 0; g < gCount; g++) {
                mins[g] = Double.POSITIVE_INFINITY;
                maxs[g] = Double.NEGATIVE_INFINITY;
                sums[g] = 0.0;
            }

            // przejście po próbkach w bloku
            for (int i = start; i < end; i++) {
                Measurement m = measurements.get(i);

                for (int g = 0; g < gCount; g++) {
                    int[] group = channelGroups.get(g);

                    double magSq = 0.0;
                    for (int chIdx : group) {
                        double v = getChannelByIndex(m, chIdx);
                        magSq += v * v;
                    }
                    double mag = Math.sqrt(magSq);

                    if (mag < mins[g]) mins[g] = mag;
                    if (mag > maxs[g]) maxs[g] = mag;
                    sums[g] += mag;
                }
            }

            List<OverviewBlockDto.GroupStats> groupStatsList = new ArrayList<>();

            for (int g = 0; g < gCount; g++) {
                double mean = sums[g] / blockSize;
                int[] group = channelGroups.get(g);

                List<Integer> channels = new ArrayList<>();
                for (int chIdx : group) {
                    channels.add(chIdx); // numer kanału: 1..8
                }

                groupStatsList.add(
                        new OverviewBlockDto.GroupStats(
                                g,          // index grupy (0,1,2)
                                channels,   // list kanałów w grupie
                                mins[g],
                                maxs[g],
                                mean
                        )
                );
            }

            blocks.add(new OverviewBlockDto(timeSec, groupStatsList));
        }

        return blocks;
    }

    /**
     * Wykrycie liczby kanałów na podstawie pierwszego pomiaru,
     * zakładamy, że kanały są od ch1 kolejno do pierwszego null.
     */
    private int detectChannelCount(List<Measurement> measurements) {
        Measurement m = measurements.get(0);
        int count = 0;

        if (m.getCh1() != null) count = 1;
        else return 0;
        if (m.getCh2() != null) count = 2;
        else return count;
        if (m.getCh3() != null) count = 3;
        else return count;
        if (m.getCh4() != null) count = 4;
        else return count;
        if (m.getCh5() != null) count = 5;
        else return count;
        if (m.getCh6() != null) count = 6;
        else return count;
        if (m.getCh7() != null) count = 7;
        else return count;
        if (m.getCh8() != null) count = 8;
        else return count;

        return count;
    }

    /**
     * Buduje grupy kanałów po 3:
     * N=3  -> [1-3]
     * N=6  -> [1-3], [4-6]
     * N=8  -> [1-3], [4-6], [7-8]
     * N=2  -> [1-2]
     * N=5  -> [1-3], [4-5]
     */
    private List<int[]> buildChannelGroups(int channelCount) {
        List<int[]> groups = new ArrayList<>();
        int start = 1;
        while (start <= channelCount) {
            int end = Math.min(start + 2, channelCount); // grupa max 3 kanały
            int size = end - start + 1;
            int[] group = new int[size];
            for (int i = 0; i < size; i++) {
                group[i] = start + i;
            }
            groups.add(group);
            start += 3;
        }
        return groups;
    }

    private double getChannelByIndex(Measurement m, int chIndex) {
        return switch (chIndex) {
            case 1 -> safe(m.getCh1());
            case 2 -> safe(m.getCh2());
            case 3 -> safe(m.getCh3());
            case 4 -> safe(m.getCh4());
            case 5 -> safe(m.getCh5());
            case 6 -> safe(m.getCh6());
            case 7 -> safe(m.getCh7());
            case 8 -> safe(m.getCh8());
            default -> 0.0;
        };
    }

    private double safe(Double v) {
        return v != null ? v : 0.0;
    }
}