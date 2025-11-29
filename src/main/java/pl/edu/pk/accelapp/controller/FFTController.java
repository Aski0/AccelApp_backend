package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.FFTResultDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.repository.MeasurementRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.service.FFTService;

import java.util.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FFTController {

    private final UploadedFileRepository uploadedFileRepository;
    private final MeasurementRepository measurementRepository;
    private final FFTService fftService;

    @GetMapping("/{fileId}/fft")
    public ResponseEntity<FFTResultDto> getFFTForAllChannels(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User principal
    ) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pliku"));

        // sprawdzenie właściciela pliku
        if (!file.getUser().getEmail().equals(principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // bierzemy pomiary, żeby wykryć ile kanałów faktycznie jest
        List<Measurement> measurements = measurementRepository.findByUploadedFileId(fileId);
        if (measurements.isEmpty()) {
            return ResponseEntity.ok(new FFTResultDto(Collections.emptyMap()));
        }

        int channelCount = detectChannelCount(measurements.get(0));
        if (channelCount == 0) {
            return ResponseEntity.ok(new FFTResultDto(Collections.emptyMap()));
        }

        // LinkedHashMap -> kanały w kolejności ch1, ch2, ...
        Map<String, List<FFTResultDto.Point>> data = new LinkedHashMap<>();

        for (int ch = 1; ch <= channelCount; ch++) {
            String channelName = "ch" + ch;          // "ch1", "ch2", ...
            List<FFTResultDto.Point> spectrum = fftService.computeFFT(fileId, channelName);
            if (spectrum != null && !spectrum.isEmpty()) {
                data.put(channelName, spectrum);
            }
        }

        return ResponseEntity.ok(new FFTResultDto(data));
    }

    /**
     * Wykrywa liczbę kanałów na podstawie pierwszego pomiaru:
     * ch1..chN aż do pierwszego null.
     */
    private int detectChannelCount(Measurement m) {
        int count = 0;

        if (m.getCh1() != null) count = 1; else return 0;
        if (m.getCh2() != null) count = 2; else return count;
        if (m.getCh3() != null) count = 3; else return count;
        if (m.getCh4() != null) count = 4; else return count;
        if (m.getCh5() != null) count = 5; else return count;
        if (m.getCh6() != null) count = 6; else return count;
        if (m.getCh7() != null) count = 7; else return count;
        if (m.getCh8() != null) count = 8; else return count;

        return count;
    }
}
