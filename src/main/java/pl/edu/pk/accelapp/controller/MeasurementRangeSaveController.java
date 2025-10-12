package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.model.MeasurementRange;
import pl.edu.pk.accelapp.service.MeasurementRangeSaveService;

@RestController
@RequestMapping("/api/ranges")
@RequiredArgsConstructor
public class MeasurementRangeSaveController {

    private final MeasurementRangeSaveService rangeSaveService;

    @PostMapping("/{fileId}")
    public ResponseEntity<MeasurementRange> saveRange(
            @PathVariable Long fileId,
            @RequestParam double start,
            @RequestParam double end,
            @RequestParam(required = false) String chartPath
    ) {
        MeasurementRange saved = rangeSaveService.saveRange(fileId, start, end, chartPath);
        return ResponseEntity.ok(saved);
    }
}
