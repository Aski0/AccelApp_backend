package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.MeasurementStatsDto;
import pl.edu.pk.accelapp.service.MeasurementStatsService;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementStatsController {

    private final MeasurementStatsService measurementStatsService;

    @GetMapping("/{fileId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long fileId, Authentication authentication) {
        try {
            MeasurementStatsDto stats = measurementStatsService.getStatsForFile(fileId, authentication);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
            }
            if (ex.getMessage().contains("File not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
