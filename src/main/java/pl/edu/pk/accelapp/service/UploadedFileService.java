package pl.edu.pk.accelapp.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;

import java.util.List;

@Service
public class UploadedFileService {
    private final UploadedFileRepository uploadedFileRepository;
    private final UserService userService;

    public UploadedFileService(UploadedFileRepository uploadedFileRepository, UserService userService) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.userService = userService;
    }

    public List<UploadedFile> getFilesForUser(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        return uploadedFileRepository.findByUser(user);
    }
}
