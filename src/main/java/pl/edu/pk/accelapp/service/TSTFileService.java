package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importuj!
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pk.accelapp.dto.TSTFileDto;
import pl.edu.pk.accelapp.model.TSTFile;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.TSTFileRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

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

    public TSTFile saveTSTFileFromJson(Long fileId, TSTFileDto dto) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Uploaded file not found"));

        User currentUser = getCurrentUser();
        if (!uploadedFile.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        TSTFile tst = new TSTFile();
        tst.setTstFilename(dto.getFilename());
        tst.setContent(dto.getContent());
        tst.setUploadedFile(uploadedFile);

        return tstFileRepository.save(tst);
    }

    public TSTFileDto getByUploadedFileId(Long fileId) {
        // 1. Znajdź plik nadrzędny
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 2. Sprawdź, czy zalogowany użytkownik jest właścicielem pliku
        User currentUser = getCurrentUser();
        if (!uploadedFile.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        // 3. Znajdź właściwy plik TST na podstawie relacji
        TSTFile tstFile = tstFileRepository.findByUploadedFileId(fileId)
                .orElseThrow(() -> new RuntimeException("TST file content not found"));

        // 4. Zmapuj encję na DTO i zwróć
        return new TSTFileDto(tstFile.getTstFilename(), tstFile.getContent());
    }
}