package pl.edu.pk.accelapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pk.accelapp.dto.FileHeaderDto;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.service.FileUploadService;
import pl.edu.pk.accelapp.service.UserService;

@RestController
@RequestMapping("/api")
public class FileUploadController {
    private final FileUploadService fileUploadService;
    private final UserService userService; // ✅ dodaj

    public FileUploadController(FileUploadService fileUploadService, UserService userService) {
        this.fileUploadService = fileUploadService;
        this.userService = userService; // ✅ zapisz
    }

    @PostMapping("/files/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        try {
            System.out.println("Otrzymany plik: " + file.getOriginalFilename());
            System.out.println("Rozmiar pliku: " + file.getSize());
            String email = principal.getUsername();
            System.out.println("Email użytkownika: " + email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

            fileUploadService.saveFile(file, user);

            return ResponseEntity.ok("Plik został zapisany dla użytkownika " + user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Błąd: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<?> getUserFiles(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Brak tokenu autoryzacji");
            }

            String email = principal.getUsername();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

            // Mapowanie do DTO
            var files = user.getUploadedFiles().stream().map(f -> new FileHeaderDto(
                    f.getId(),
                    f.getFilename(),
                    f.getUploadedAt(),
                    f.getMeasurement().size() // liczba próbek
            )).toList();

            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Błąd: " + e.getMessage());
        }
    }
}



