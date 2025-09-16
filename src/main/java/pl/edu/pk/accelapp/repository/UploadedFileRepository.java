package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pk.accelapp.model.UploadedFile;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByUserId(Long userId);
}
