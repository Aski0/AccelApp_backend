package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.service.MeasurementOverviewService;
import pl.edu.pk.accelapp.service.MeasurementOverviewService.OverviewBlock;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementOverviewController {

    private final MeasurementOverviewService overviewService;

    @GetMapping("/{fileId}/overview")
    public ResponseEntity<?> getOverview(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "10000") int blockSize,
            Authentication authentication
    ) {
        try {
            List<OverviewBlock> blocks = overviewService.getOverview(fileId, authentication, blockSize);
            return ResponseEntity.ok(blocks);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Access denied")) {
                return ResponseEntity.status(403).body(ex.getMessage());
            }
            if (ex.getMessage().contains("File not found")) {
                return ResponseEntity.status(404).body(ex.getMessage());
            }
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }
}
