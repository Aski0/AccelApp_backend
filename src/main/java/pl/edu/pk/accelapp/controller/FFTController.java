package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.FFTResultDto;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.service.FFTService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FFTController {

    private final UploadedFileRepository uploadedFileRepository;
    private final FFTService fftService;

    @GetMapping("/{fileId}/fft")
    public ResponseEntity<FFTResultDto> getFFTForAllChannels(
            @PathVariable Long fileId,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pliku"));

        if (!file.getUser().getEmail().equals(principal.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        Map<String, List<FFTResultDto.Point>> data = Map.of(
                "ox", fftService.computeFFT(fileId, "ox"),
                "oy", fftService.computeFFT(fileId, "oy"),
                "oz", fftService.computeFFT(fileId, "oz"),
                "ch1", fftService.computeFFT(fileId, "ch1"),
                "ch2", fftService.computeFFT(fileId, "ch2"),
                "ch3", fftService.computeFFT(fileId, "ch3")
        );

        return ResponseEntity.ok(new FFTResultDto(data));
    }
}
