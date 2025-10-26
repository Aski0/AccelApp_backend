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
        SELECT 
            COUNT(*) AS cnt,

            -- CH1
            MIN(ch1), MAX(ch1), AVG(ch1), STDDEV_POP(ch1),
            SQRT(AVG(ch1 * ch1)) AS rms_ch1,
            (MAX(ch1) - MIN(ch1)) AS peak_to_peak_ch1,
            VAR_POP(ch1) AS variance_ch1,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch1) AS median_ch1,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch1) AS p05_ch1,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch1) AS p95_ch1,

            -- CH2
            MIN(ch2), MAX(ch2), AVG(ch2), STDDEV_POP(ch2),
            SQRT(AVG(ch2 * ch2)) AS rms_ch2,
            (MAX(ch2) - MIN(ch2)) AS peak_to_peak_ch2,
            VAR_POP(ch2) AS variance_ch2,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch2) AS median_ch2,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch2) AS p05_ch2,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch2) AS p95_ch2,

            -- CH3
            MIN(ch3), MAX(ch3), AVG(ch3), STDDEV_POP(ch3),
            SQRT(AVG(ch3 * ch3)) AS rms_ch3,
            (MAX(ch3) - MIN(ch3)) AS peak_to_peak_ch3,
            VAR_POP(ch3) AS variance_ch3,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch3) AS median_ch3,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch3) AS p05_ch3,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch3) AS p95_ch3,

            -- OX
            MIN(ox), MAX(ox), AVG(ox), STDDEV_POP(ox),
            SQRT(AVG(ox * ox)) AS rms_ox,
            (MAX(ox) - MIN(ox)) AS peak_to_peak_ox,
            VAR_POP(ox) AS variance_ox,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ox) AS median_ox,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ox) AS p05_ox,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ox) AS p95_ox,

            -- OY
            MIN(oy), MAX(oy), AVG(oy), STDDEV_POP(oy),
            SQRT(AVG(oy * oy)) AS rms_oy,
            (MAX(oy) - MIN(oy)) AS peak_to_peak_oy,
            VAR_POP(oy) AS variance_oy,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY oy) AS median_oy,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY oy) AS p05_oy,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY oy) AS p95_oy,

            -- OZ
            MIN(oz), MAX(oz), AVG(oz), STDDEV_POP(oz),
            SQRT(AVG(oz * oz)) AS rms_oz,
            (MAX(oz) - MIN(oz)) AS peak_to_peak_oz,
            VAR_POP(oz) AS variance_oz,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY oz) AS median_oz,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY oz) AS p05_oz,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY oz) AS p95_oz

        FROM measurements
        WHERE uploaded_file_id = :fileId
    """, nativeQuery = true)
    Object getStatsForFile(@Param("fileId") Long fileId);
}
