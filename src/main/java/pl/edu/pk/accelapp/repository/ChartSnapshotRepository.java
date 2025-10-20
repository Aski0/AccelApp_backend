package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pk.accelapp.model.ChartSnapshot;

import java.util.List;
import java.util.Optional;

public interface ChartSnapshotRepository extends JpaRepository<ChartSnapshot, Long> {
    List<ChartSnapshot> findByUploadedFileId(Long fileId);
    Optional<ChartSnapshot> findByIdAndUploadedFileId(Long id, Long fileId);
}
