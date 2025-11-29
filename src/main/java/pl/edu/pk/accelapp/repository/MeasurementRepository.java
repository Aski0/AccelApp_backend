package pl.edu.pk.accelapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.pk.accelapp.model.Measurement;

import java.util.List;
import java.util.stream.Stream;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    // ───────────── Podstawowe zapytania ─────────────

    // Bez sortowania – jak potrzebujesz "po prostu listy"
    List<Measurement> findByUploadedFileId(Long fileId);

    // Posortowane po czasie – używaj tego do overview / zakresów
    List<Measurement> findByUploadedFileIdOrderByTimeAsc(Long fileId);

    Page<Measurement> findByUploadedFileIdOrderByTimeAsc(Long fileId, Pageable pageable);

    long countByUploadedFileId(Long fileId);

    // min/max time – może zostać jak jest, bo to prosta agregacja
    @Query("SELECT MIN(m.time), MAX(m.time) FROM Measurement m WHERE m.uploadedFile.id = :fileId")
    Object[] findMinAndMaxTimeByFileId(@Param("fileId") Long fileId);

    // Stream – DOKŁADAMY sortowanie po czasie
    @Query("SELECT m FROM Measurement m WHERE m.uploadedFile.id = :fileId ORDER BY m.time ASC")
    Stream<Measurement> streamByUploadedFileId(@Param("fileId") Long fileId);


    // ───────────── Statystyki dla dynamicznych kanałów ch1..ch8 ─────────────
    //
    // - OX/OY/OZ wyrzucamy całkowicie
    // - dla ch1..ch8 mamy: count, min, max, avg, stddev, rms, peak-to-peak, variance, median, p05, p95
    // - NULL-e nie psują statystyk (agg funkcje Postgresa je ignorują)

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

            -- CH4
            MIN(ch4), MAX(ch4), AVG(ch4), STDDEV_POP(ch4),
            SQRT(AVG(ch4 * ch4)) AS rms_ch4,
            (MAX(ch4) - MIN(ch4)) AS peak_to_peak_ch4,
            VAR_POP(ch4) AS variance_ch4,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch4) AS median_ch4,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch4) AS p05_ch4,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch4) AS p95_ch4,

            -- CH5
            MIN(ch5), MAX(ch5), AVG(ch5), STDDEV_POP(ch5),
            SQRT(AVG(ch5 * ch5)) AS rms_ch5,
            (MAX(ch5) - MIN(ch5)) AS peak_to_peak_ch5,
            VAR_POP(ch5) AS variance_ch5,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch5) AS median_ch5,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch5) AS p05_ch5,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch5) AS p95_ch5,

            -- CH6
            MIN(ch6), MAX(ch6), AVG(ch6), STDDEV_POP(ch6),
            SQRT(AVG(ch6 * ch6)) AS rms_ch6,
            (MAX(ch6) - MIN(ch6)) AS peak_to_peak_ch6,
            VAR_POP(ch6) AS variance_ch6,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch6) AS median_ch6,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch6) AS p05_ch6,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch6) AS p95_ch6,

            -- CH7
            MIN(ch7), MAX(ch7), AVG(ch7), STDDEV_POP(ch7),
            SQRT(AVG(ch7 * ch7)) AS rms_ch7,
            (MAX(ch7) - MIN(ch7)) AS peak_to_peak_ch7,
            VAR_POP(ch7) AS variance_ch7,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch7) AS median_ch7,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch7) AS p05_ch7,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch7) AS p95_ch7,

            -- CH8
            MIN(ch8), MAX(ch8), AVG(ch8), STDDEV_POP(ch8),
            SQRT(AVG(ch8 * ch8)) AS rms_ch8,
            (MAX(ch8) - MIN(ch8)) AS peak_to_peak_ch8,
            VAR_POP(ch8) AS variance_ch8,
            PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY ch8) AS median_ch8,
            PERCENTILE_CONT(0.05) WITHIN GROUP (ORDER BY ch8) AS p05_ch8,
            PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY ch8) AS p95_ch8

        FROM measurements
        WHERE uploaded_file_id = :fileId
    """, nativeQuery = true)
    Object getStatsForFile(@Param("fileId") Long fileId);
}
