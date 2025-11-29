package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.dto.MeasurementStatsDto;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.MeasurementRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementStatsService {

    private final MeasurementRepository measurementRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public MeasurementStatsDto getStatsForFile(Long fileId, Authentication authentication) {

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User currentUser = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: file does not belong to user");
        }

        Object result = measurementRepository.getStatsForFile(fileId);
        if (result == null) {
            // brak danych w pliku
            return new MeasurementStatsDto(file.getFilename(), 0L, List.of());
        }

        Object[] r = (Object[]) result;

        long count = safeLong(r[0]);
        if (count == 0L) {
            return new MeasurementStatsDto(file.getFilename(), 0L, List.of());
        }

        // struktura: [0] = count, potem 8 kanałów * 10 metryk = 80 wartości
        final int metricsPerChannel = 10;
        final int maxChannels = 8;

        List<MeasurementStatsDto.ChannelStats> channels = new ArrayList<>();

        for (int ch = 0; ch < maxChannels; ch++) {
            int baseIndex = 1 + ch * metricsPerChannel;

            // sprawdź, czy kanał ma jakiekolwiek nie-nullowe statystyki
            boolean allNull = true;
            for (int k = 0; k < metricsPerChannel; k++) {
                if (r[baseIndex + k] != null) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                // ten kanał realnie nie istnieje w danych -> pomijamy
                continue;
            }

            String channelName = "ch" + (ch + 1);

            Double min       = safeDouble(r[baseIndex]);       // 0
            Double max       = safeDouble(r[baseIndex + 1]);   // 1
            Double mean      = safeDouble(r[baseIndex + 2]);   // 2
            Double stdDev    = safeDouble(r[baseIndex + 3]);   // 3
            Double rms       = safeDouble(r[baseIndex + 4]);   // 4
            Double peakToPeak= safeDouble(r[baseIndex + 5]);   // 5
            Double variance  = safeDouble(r[baseIndex + 6]);   // 6
            Double median    = safeDouble(r[baseIndex + 7]);   // 7
            Double p05       = safeDouble(r[baseIndex + 8]);   // 8
            Double p95       = safeDouble(r[baseIndex + 9]);   // 9

            channels.add(new MeasurementStatsDto.ChannelStats(
                    channelName,
                    min,
                    max,
                    mean,
                    stdDev,
                    rms,
                    peakToPeak,
                    variance,
                    median,
                    p05,
                    p95
            ));
        }

        return new MeasurementStatsDto(file.getFilename(), count, channels);
    }

    private Double safeDouble(Object obj) {
        return obj != null ? ((Number) obj).doubleValue() : null;
    }

    private Long safeLong(Object obj) {
        return obj != null ? ((Number) obj).longValue() : 0L;
    }
}
