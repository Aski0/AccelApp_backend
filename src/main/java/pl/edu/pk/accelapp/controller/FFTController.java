package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.FFTPointDto;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.service.FFTService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FFTController {

    private final UploadedFileRepository uploadedFileRepository;
    private final FFTService fftService;

    @GetMapping("/{fileId}/fft/{channel}")
    public ResponseEntity<List<FFTPointDto>> getFFT(
            @PathVariable Long fileId,
            @PathVariable String channel,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pliku"));

        if (!file.getUser().getEmail().equals(principal.getUsername())) {
            return ResponseEntity.status(403).build();
        }

        List<FFTPointDto> fftData = fftService.computeFFT(fileId, channel);
        return ResponseEntity.ok(fftData);
    }
}
