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
     * Dla każdego bloku liczymy min/max/mean magnitude liczonych
     * ze wszystkich istniejących kanałów (ch1..chN), z pominięciem pustych kanałów.
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

        // Ile kanałów faktycznie jest (ch1..chN, do pierwszego null)
        int channelCount = detectChannelCount(measurements);
        if (channelCount == 0) {
            return Collections.emptyList();
        }

        List<OverviewBlockDto> blocks = new ArrayList<>();

        for (int b = 0; b < nBlocks; b++) {
            int start = b * blockSize;
            int end = start + blockSize;

            // czas bloku – z pierwszej próbki
            double timeSec = safe(measurements.get(start).getTime());

            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            double sum = 0.0;
            int validCount = 0;   // ile próbek faktycznie policzyliśmy

            for (int i = start; i < end; i++) {
                Measurement m = measurements.get(i);

                double magSq = 0.0;
                boolean anyValue = false;

                // bierzemy wszystkie istniejące kanały (1..channelCount)
                for (int chIdx = 1; chIdx <= channelCount; chIdx++) {
                    Double raw = getChannelRaw(m, chIdx);
                    if (raw != null) {
                        anyValue = true;
                        magSq += raw * raw;
                    }
                }

                if (!anyValue) {
                    // cała próbka "pusta" – pomijamy
                    continue;
                }

                double mag = Math.sqrt(magSq);

                if (mag < min) min = mag;
                if (mag > max) max = mag;
                sum += mag;
                validCount++;
            }

            if (validCount == 0) {
                // brak danych w bloku – można opcjonalnie pominąć ten blok
                blocks.add(new OverviewBlockDto(timeSec, 0.0, 0.0, 0.0));
            } else {
                double mean = sum / validCount;
                blocks.add(new OverviewBlockDto(timeSec, min, max, mean));
            }
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

        if (m.getCh1() != null) count = 1; else return 0;
        if (m.getCh2() != null) count = 2; else return count;
        if (m.getCh3() != null) count = 3; else return count;
        if (m.getCh4() != null) count = 4; else return count;
        if (m.getCh5() != null) count = 5; else return count;
        if (m.getCh6() != null) count = 6; else return count;
        if (m.getCh7() != null) count = 7; else return count;
        if (m.getCh8() != null) count = 8; else return count;

        return count;
    }

    /**
     * Zwraca surową wartość kanału (może być null).
     */
    private Double getChannelRaw(Measurement m, int chIndex) {
        return switch (chIndex) {
            case 1 -> m.getCh1();
            case 2 -> m.getCh2();
            case 3 -> m.getCh3();
            case 4 -> m.getCh4();
            case 5 -> m.getCh5();
            case 6 -> m.getCh6();
            case 7 -> m.getCh7();
            case 8 -> m.getCh8();
            default -> null;
        };
    }

    private double safe(Double v) {
        return v != null ? v : 0.0;
    }
}
