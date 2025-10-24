package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.MeasurementDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.MeasurementRange;
import pl.edu.pk.accelapp.service.MeasurementRangeService;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementRangeController {

    private final MeasurementRangeService measurementRangeService;

    // ✅ 1. Pomiar w określonym zakresie
    @GetMapping("/{fileId}/range")
    public List<MeasurementDto> getMeasurementsInRange(
            @PathVariable Long fileId,
            @RequestParam double start,
            @RequestParam double end,
            Authentication authentication
    ) {
        List<Measurement> measurements = measurementRangeService.getMeasurementsInRange(fileId, authentication, start, end);
        return measurements.stream().map(MeasurementDto::new).toList();
    }

    // ✅ 2. Lista zapisanych zakresów dla pliku
    @GetMapping("/{fileId}/ranges")
    public List<MeasurementRange> getRangesForFile(
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        return measurementRangeService.getRangesForFile(fileId, authentication);
    }

    // ✅ 3. Zapisz nowy zakres
    @PostMapping("/{fileId}/ranges")
    public ResponseEntity<MeasurementRange> saveRange(
            @PathVariable Long fileId,
            @RequestParam double start,
            @RequestParam double end,
            @RequestParam(required = false) String chartPath,
            Authentication authentication
    ) {
        MeasurementRange saved = measurementRangeService.saveRange(fileId, start, end, chartPath, authentication);
        return ResponseEntity.ok(saved);
    }

    // ✅ 4. Usuń istniejący zakres
    @DeleteMapping("/ranges/{rangeId}")
    public ResponseEntity<Void> deleteRange(
            @PathVariable Long rangeId,
            Authentication authentication
    ) {
        measurementRangeService.deleteRange(rangeId, authentication);
        return ResponseEntity.noContent().build();
    }

}
