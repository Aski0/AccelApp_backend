package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.MeasurementDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.service.MeasurementRangeService;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementRangeController {

    private final MeasurementRangeService measurementRangeService;

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
}
