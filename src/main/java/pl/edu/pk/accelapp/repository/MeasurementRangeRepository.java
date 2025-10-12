package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.pk.accelapp.model.MeasurementRange;

import java.util.List;

@Repository
public interface MeasurementRangeRepository extends JpaRepository<MeasurementRange, Long> {
    List<MeasurementRange> findByUploadedFileId(Long fileId);
}
