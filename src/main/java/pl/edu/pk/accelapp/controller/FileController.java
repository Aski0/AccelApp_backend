package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pk.accelapp.dto.FileHeaderDto;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;
import pl.edu.pk.accelapp.service.FileService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @GetMapping
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

    @PatchMapping("/{id}")
    public FileHeaderDto renameFile(@PathVariable Long id,
                                    @RequestBody FileHeaderDto renameDto,
                                    Principal principal) {
        UploadedFile updatedFile = fileService.renameFile(id, renameDto.getFilename(), principal.getName());
        return new FileHeaderDto(
                updatedFile.getId(),
                updatedFile.getFilename(),
                updatedFile.getUploadedAt(),
                uploadedFileRepository.countMeasurementsByFileId(updatedFile.getId())
        );
    }

    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable Long id, Principal principal) {
        fileService.deleteFile(id, principal.getName());
    }
}
