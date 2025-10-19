package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pk.accelapp.dto.ChartSnapshotDto;
import pl.edu.pk.accelapp.model.ChartSnapshot;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.ChartSnapshotRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ChartSnapshotService {

    private final ChartSnapshotRepository chartSnapshotRepository;
    private final UploadedFileRepository uploadedFileRepository;

    private final String CHART_DIR = "chart_images"; // katalog w projekcie / dysku
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
    public ChartSnapshot saveChart(ChartSnapshotDto dto) throws Exception {

        UploadedFile file = uploadedFileRepository.findById(dto.getFileId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pliku"));

        // dekodowanie Base64
        String base64Data = dto.getBase64Image().split(",")[1]; // usuwa nagłówek data:image/png;base64,
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // przygotowanie katalogu
        File dir = new File(CHART_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = dto.getName();
        File outputFile = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(imageBytes);
        }

        // zapis w bazie
        ChartSnapshot snapshot = new ChartSnapshot();
        snapshot.setUploadedFile(file);
        snapshot.setName(fileName);
        snapshot.setType(dto.getType());
        snapshot.setFilePath(outputFile.getAbsolutePath());

        return chartSnapshotRepository.save(snapshot);
    }
}
