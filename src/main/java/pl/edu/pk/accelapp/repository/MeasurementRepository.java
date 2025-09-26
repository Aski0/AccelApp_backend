package pl.edu.pk.accelapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.UploadedFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByUploadedFileId(Long fileId);
    Page<Measurement> findByUploadedFileId(Long fileId, Pageable pageable);

    long countByUploadedFileId(Long fileId);


    @Query("SELECT MIN(m.time), MAX(m.time) FROM Measurement m WHERE m.uploadedFile.id = :fileId")
    Object[] findMinAndMaxTimeByFileId(@Param("fileId") Long fileId);

    @Query("SELECT m FROM Measurement m WHERE m.uploadedFile.id = :fileId")
    Stream<Measurement> streamByUploadedFileId(@Param("fileId") Long fileId);


    @Query(value = """
    SELECT COUNT(*) AS cnt,
           MIN(ch1), MAX(ch1), AVG(ch1), STDDEV_POP(ch1),
           MIN(ch2), MAX(ch2), AVG(ch2), STDDEV_POP(ch2),
           MIN(ch3), MAX(ch3), AVG(ch3), STDDEV_POP(ch3),
           MIN(ox), MAX(ox), AVG(ox), STDDEV_POP(ox),
           MIN(oy), MAX(oy), AVG(oy), STDDEV_POP(oy),
           MIN(oz), MAX(oz), AVG(oz), STDDEV_POP(oz)
    FROM measurements
    WHERE uploaded_file_id = :fileId
""", nativeQuery = true)
    Object getStatsForFile(@Param("fileId") Long fileId);
}
