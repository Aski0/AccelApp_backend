package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.pk.accelapp.model.TSTFile;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;

import java.util.List;
import java.util.Optional;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByUser(User user);

    @Query("SELECT COUNT(m) FROM Measurement m WHERE m.uploadedFile.id = :fileId")
    long countMeasurementsByFileId(@Param("fileId") Long fileId);
    @Query("SELECT t FROM TSTFile t WHERE t.uploadedFile.id = :fileId")
    Optional<TSTFile> findTSTByUploadedFileId(@Param("fileId") Long fileId);
}
