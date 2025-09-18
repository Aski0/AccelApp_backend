package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.MeasurementDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.service.MeasurementService;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    @GetMapping("/{fileId}")
    public Page<MeasurementDto> getMeasurementsByFile(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return measurementService.getMeasurementsByFilePaged(fileId, PageRequest.of(page, size))
                .map(m -> new MeasurementDto(m));
    }
}
