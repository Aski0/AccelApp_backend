package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pk.accelapp.dto.FileHeaderDto;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    @GetMapping("/files")
    public List<FileHeaderDto> getUserFiles(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return uploadedFileRepository.findByUser(user).stream()
                .map(file -> new FileHeaderDto(
                        file.getId(),
                        file.getFilename(),
                        file.getUploadedAt(),
                        uploadedFileRepository.countMeasurementsByFileId(file.getId())
                ))
                .toList();
    }
    @GetMapping("/files/{fileId}")
    public FileHeaderDto getFileById(@PathVariable Long fileId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // zabezpieczenie – żeby użytkownik nie mógł pobierać cudzych plików
        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return new FileHeaderDto(
                file.getId(),
                file.getFilename(),
                file.getUploadedAt(),
                uploadedFileRepository.countMeasurementsByFileId(file.getId())
        );
    }
}
