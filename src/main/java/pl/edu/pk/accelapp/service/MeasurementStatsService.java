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

        return new MeasurementStatsDto(
                safeLong(r[0]),
                safeDouble(r[1]), safeDouble(r[2]), safeDouble(r[3]), safeDouble(r[4]),
                safeDouble(r[5]), safeDouble(r[6]), safeDouble(r[7]), safeDouble(r[8]),
                safeDouble(r[9]), safeDouble(r[10]), safeDouble(r[11]), safeDouble(r[12]),
                safeDouble(r[13]), safeDouble(r[14]), safeDouble(r[15]), safeDouble(r[16]),
                safeDouble(r[17]), safeDouble(r[18]), safeDouble(r[19]), safeDouble(r[20]),
                safeDouble(r[21]), safeDouble(r[22]), safeDouble(r[23]), safeDouble(r[24])
        );
    }


    private Double safeDouble(Object obj) {
        return obj != null ? ((Number) obj).doubleValue() : 0.0;
    }

    private Long safeLong(Object obj) {
        return obj != null ? ((Number) obj).longValue() : 0L;
    }

}
