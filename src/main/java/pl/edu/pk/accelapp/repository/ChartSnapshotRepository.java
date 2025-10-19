package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pk.accelapp.model.ChartSnapshot;

import java.util.List;

public interface ChartSnapshotRepository extends JpaRepository<ChartSnapshot, Long> {
    List<ChartSnapshot> findByUploadedFileId(Long fileId);
}
