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

        Object[] r = (Object[]) measurementRepository.getStatsForFile(fileId);

        // utworzenie DTO z danymi statystycznymi
        MeasurementStatsDto dto = new MeasurementStatsDto(
                file.getFilename(),
                safeLong(r[0]),

                // CH1
                safeDouble(r[1]), safeDouble(r[2]), safeDouble(r[3]), safeDouble(r[4]),
                safeDouble(r[5]), safeDouble(r[6]), safeDouble(r[7]),
                safeDouble(r[8]), safeDouble(r[9]), safeDouble(r[10]),

                // CH2
                safeDouble(r[11]), safeDouble(r[12]), safeDouble(r[13]), safeDouble(r[14]),
                safeDouble(r[15]), safeDouble(r[16]), safeDouble(r[17]),
                safeDouble(r[18]), safeDouble(r[19]), safeDouble(r[20]),

                // CH3
                safeDouble(r[21]), safeDouble(r[22]), safeDouble(r[23]), safeDouble(r[24]),
                safeDouble(r[25]), safeDouble(r[26]), safeDouble(r[27]),
                safeDouble(r[28]), safeDouble(r[29]), safeDouble(r[30]),

                // OX
                safeDouble(r[31]), safeDouble(r[32]), safeDouble(r[33]), safeDouble(r[34]),
                safeDouble(r[35]), safeDouble(r[36]), safeDouble(r[37]),
                safeDouble(r[38]), safeDouble(r[39]), safeDouble(r[40]),

                // OY
                safeDouble(r[41]), safeDouble(r[42]), safeDouble(r[43]), safeDouble(r[44]),
                safeDouble(r[45]), safeDouble(r[46]), safeDouble(r[47]),
                safeDouble(r[48]), safeDouble(r[49]), safeDouble(r[50]),

                // OZ
                safeDouble(r[51]), safeDouble(r[52]), safeDouble(r[53]), safeDouble(r[54]),
                safeDouble(r[55]), safeDouble(r[56]), safeDouble(r[57]),
                safeDouble(r[58]), safeDouble(r[59]), safeDouble(r[60])
        );
        return dto;
    }

    private Double safeDouble(Object obj) {
        return obj != null ? ((Number) obj).doubleValue() : 0.0;
    }

    private Long safeLong(Object obj) {
        return obj != null ? ((Number) obj).longValue() : 0L;
    }
}
