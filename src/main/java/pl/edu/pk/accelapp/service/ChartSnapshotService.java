package pl.edu.pk.accelapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartSnapshotService {

    private final ChartSnapshotRepository chartSnapshotRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    private static final String CHART_DIR = "chart_images"; // katalog na dysku

    // Pobranie aktualnie zalogowanego użytkownika
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Brak uwierzytelnienia");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
    }

    // Zapis nowego wykresu
    @Transactional
    public ChartSnapshotDto saveChart(ChartSnapshotDto dto) {
        User currentUser = getCurrentUser();

        UploadedFile file = uploadedFileRepository.findById(dto.getFileId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pliku"));

        if (!file.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Brak dostępu do pliku innego użytkownika");
        }

        if (dto.getBase64Image() == null || dto.getBase64Image().isEmpty()) {
            throw new RuntimeException("Brak danych Base64 w DTO");
        }

        // Dekodowanie Base64
        String base64Data = dto.getBase64Image().contains(",")
                ? dto.getBase64Image().split(",")[1]
                : dto.getBase64Image();

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // Tworzenie katalogu, jeśli nie istnieje
        File dir = new File(CHART_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Zapis pliku na dysku
        String fileName = dto.getName() + ".png";
        File outputFile = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Błąd zapisu pliku: " + e.getMessage());
        }

        // Zapis w bazie
        ChartSnapshot snapshot = new ChartSnapshot();
        snapshot.setUploadedFile(file);
        snapshot.setName(dto.getName());
        snapshot.setType(dto.getType());
        snapshot.setFilePath(outputFile.getAbsolutePath());

        ChartSnapshot saved = chartSnapshotRepository.save(snapshot);

        return ChartSnapshotDto.fromEntity(saved);
    }

    // Lista wykresów dla danego pliku
    @Transactional
    public List<ChartSnapshotDto> getChartsForFile(Long fileId) {
        User currentUser = getCurrentUser();

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pliku"));

        if (!file.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Brak dostępu do wykresów innego użytkownika");
        }

        List<ChartSnapshot> snapshots = chartSnapshotRepository.findByUploadedFileId(fileId);
        return snapshots.stream()
                .map(ChartSnapshotDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Pobranie pojedynczego wykresu
    @Transactional
    public ChartSnapshotDto getChartById(Long fileId, Long chartId) {
        User currentUser = getCurrentUser();

        ChartSnapshot chart = chartSnapshotRepository
                .findByIdAndUploadedFileId(chartId, fileId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wykresu o ID: " + chartId));

        if (!chart.getUploadedFile().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Brak dostępu do wykresu innego użytkownika");
        }

        return ChartSnapshotDto.fromEntity(chart);
    }

    // Usuwanie wykresu
    @Transactional
    public void deleteChart(Long fileId, Long chartId) {
        User currentUser = getCurrentUser();

        ChartSnapshot chart = chartSnapshotRepository
                .findByIdAndUploadedFileId(chartId, fileId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wykresu o ID: " + chartId));

        if (!chart.getUploadedFile().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Brak dostępu do usuwania wykresu innego użytkownika");
        }

        // Usunięcie pliku z dysku
        if (chart.getFilePath() != null) {
            File f = new File(chart.getFilePath());
            if (f.exists()) f.delete();
        }

        chartSnapshotRepository.delete(chart);
    }
}
