package pl.edu.pk.accelapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.service.FileUploadService;
import pl.edu.pk.accelapp.service.UserService;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    private final FileUploadService fileUploadService;
    private final UserService userService; // ✅ dodaj

    public FileUploadController(FileUploadService fileUploadService, UserService userService) {
        this.fileUploadService = fileUploadService;
        this.userService = userService; // ✅ zapisz
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        try {
            String email = principal.getUsername();
            User user = userService.findByEmail(email) // ✅ tu wywołujesz na bean-ie
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

            fileUploadService.saveFile(file, user);

            return ResponseEntity.ok("Plik został zapisany dla użytkownika " + user.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Błąd: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<?> getUserFiles(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        try {
            String email = principal.getUsername();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

            return ResponseEntity.ok(user.getUploadedFiles());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Błąd: " + e.getMessage());
        }
    }
}



