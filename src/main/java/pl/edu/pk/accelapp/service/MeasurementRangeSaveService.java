package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.model.MeasurementRange;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.MeasurementRangeRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MeasurementRangeSaveService {
    private final UploadedFileRepository uploadedFileRepository;
    private final MeasurementRangeRepository rangeRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public MeasurementRange saveRange(Long fileId, double start, double end, String chartPath) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        MeasurementRange range = new MeasurementRange();
        range.setUploadedFile(file);
        range.setStart(start);
        range.setEnd(end);
        range.setChartImagePath(chartPath);

        return rangeRepository.save(range);
    }
    // 🔹 Usuń zakres (z kontrolą właściciela)
    public void deleteRange(Long rangeId, Authentication authentication) {
        MeasurementRange range = rangeRepository.findById(rangeId)
                .orElseThrow(() -> new RuntimeException("Range not found"));

        User currentUser = getCurrentUser(authentication);
        if (!range.getUploadedFile().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: range does not belong to user");
        }

        rangeRepository.delete(range);
    }
}