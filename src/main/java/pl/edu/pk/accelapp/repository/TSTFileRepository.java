package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pk.accelapp.model.TSTFile;

import java.util.Optional;

public interface TSTFileRepository extends JpaRepository<TSTFile, Long> {

    Optional<TSTFile> findByUploadedFileId(Long uploadedFileId);
}
