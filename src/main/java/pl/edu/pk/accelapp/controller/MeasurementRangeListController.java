package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.model.MeasurementRange;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.MeasurementRangeRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementRangeListController {

    private final MeasurementRangeRepository measurementRangeRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/{fileId}/ranges")
    public List<MeasurementRange> getRangesForFile(
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User user = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: file does not belong to user");
        }

        return measurementRangeRepository.findByUploadedFileId(fileId);
    }
}
