package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pk.accelapp.model.Measurement;

import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByUploadedFileId(Long fileId);
}
