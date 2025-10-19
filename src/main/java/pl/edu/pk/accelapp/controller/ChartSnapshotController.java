package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.ChartSnapshotDto;
import pl.edu.pk.accelapp.model.ChartSnapshot;
import pl.edu.pk.accelapp.service.ChartSnapshotService;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartSnapshotController {

    private final ChartSnapshotService chartSnapshotService;

    @PostMapping("/save")
    public ResponseEntity<?> saveChart(@RequestBody ChartSnapshotDto dto) {
        try {
            ChartSnapshot snapshot = chartSnapshotService.saveChart(dto);
            return ResponseEntity.ok(snapshot);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Błąd zapisu wykresu: " + e.getMessage());
        }
    }
}
