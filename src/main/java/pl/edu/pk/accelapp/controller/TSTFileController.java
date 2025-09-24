package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.TSTFileDto;
import pl.edu.pk.accelapp.service.TSTFileService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class TSTFileController {

    private final TSTFileService tstFileService;

    @PostMapping("/{fileId}/tst/json")
    public ResponseEntity<?> uploadTSTFileAsJson(
            @PathVariable Long fileId,
            @RequestBody TSTFileDto dto) {
        try {
            TSTFileDto saved = tstFileService.saveTSTFileFromJson(fileId, dto);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
            }
            if (ex.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/{fileId}/tst")
    public ResponseEntity<?> getTSTFile(@PathVariable Long fileId) {
        try {
            TSTFileDto fileDto = tstFileService.getByUploadedFileId(fileId);
            return ResponseEntity.ok(fileDto);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
            }
            if (ex.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
