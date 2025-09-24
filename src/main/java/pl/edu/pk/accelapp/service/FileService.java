package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    public UploadedFile renameFile(Long fileId, String newName, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        file.setFilename(newName);
        return uploadedFileRepository.save(file);
    }

    public void deleteFile(Long fileId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        uploadedFileRepository.delete(file);
    }


        public Optional<UploadedFile> getFileById(Long id) {
            return uploadedFileRepository.findById(id);
        }

}
