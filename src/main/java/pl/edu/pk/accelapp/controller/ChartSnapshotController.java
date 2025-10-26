package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.ChartSnapshotDto;
import pl.edu.pk.accelapp.model.ChartSnapshot;
import pl.edu.pk.accelapp.repository.ChartSnapshotRepository;
import pl.edu.pk.accelapp.service.ChartSnapshotService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartSnapshotController {

    private final ChartSnapshotService chartSnapshotService;
    private final ChartSnapshotRepository chartSnapshotRepository;

    @PostMapping("/save")
    public ResponseEntity<ChartSnapshotDto> saveChart(@RequestBody ChartSnapshotDto dto) throws Exception {
        ChartSnapshotDto savedDto = chartSnapshotService.saveChart(dto);
        return ResponseEntity.ok(savedDto);
    }

    @GetMapping("/file/{fileId}/list")
    public ResponseEntity<?> getChartsForFile(@PathVariable Long fileId) {
        try {
            return ResponseEntity.ok(chartSnapshotService.getChartsForFile(fileId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Błąd pobierania wykresów: " + e.getMessage());
        }
    }

    // 🔹 NOWY ENDPOINT
    @GetMapping("/{fileId}/{chartId}")
    public ResponseEntity<?> getChartById(@PathVariable Long fileId, @PathVariable Long chartId) {
        try {
            return ResponseEntity.ok(chartSnapshotService.getChartById(fileId, chartId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(403).body("Brak dostępu lub błąd: " + e.getMessage());
        }
    }

    @GetMapping("/image/{chartId}")
    public ResponseEntity<byte[]> getChartImage(@PathVariable Long chartId) throws IOException {
        ChartSnapshot snapshot = chartSnapshotRepository.findById(chartId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wykresu o ID: " + chartId));

        File file = new File(snapshot.getFilePath());
        if (!file.exists()) throw new RuntimeException("Plik nie istnieje: " + snapshot.getFilePath());

        byte[] imageBytes = Files.readAllBytes(file.toPath());
        HttpHeaders headers = new HttpHeaders();

        String mimeType = Files.probeContentType(file.toPath());
        headers.setContentType(MediaType.parseMediaType(mimeType != null ? mimeType : "image/png"));

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}

