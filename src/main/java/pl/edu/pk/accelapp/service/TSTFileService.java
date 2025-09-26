package pl.edu.pk.accelapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.dto.TSTFileDto;
import pl.edu.pk.accelapp.model.TSTFile;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.TSTFileRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TSTFileService {

    private final TSTFileRepository tstFileRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Brak uwierzytelnienia");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
    }

    @Transactional
    public TSTFileDto saveTSTFileFromJson(Long fileId, TSTFileDto dto) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Uploaded file not found"));

        User currentUser = getCurrentUser();
        if (!uploadedFile.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        TSTFile tst = new TSTFile();
        tst.setFilename(dto.getFilename());
        tst.setContent(dto.getContent());
        tst.setUploadedFile(uploadedFile);

        TSTFile saved = tstFileRepository.save(tst);

        return new TSTFileDto(saved.getFilename(), saved.getContent());
    }

    @Transactional
    public TSTFileDto getByUploadedFileId(Long fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        User currentUser = getCurrentUser();
        if (!uploadedFile.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        TSTFile tstFileEntity = tstFileRepository.findByUploadedFileId(fileId)
                .orElseThrow(() -> new RuntimeException("TST file content not found for file id: " + fileId));

        return new TSTFileDto(
                tstFileEntity.getFilename(),
                tstFileEntity.getContent()
        );
    }
}
