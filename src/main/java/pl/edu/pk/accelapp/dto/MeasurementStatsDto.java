package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementStatsDto {
    private String fileName;
    private Long count;


    // --- CH1 ---
    private Double minCh1;
    private Double maxCh1;
    private Double meanCh1;
    private Double stdCh1;
    private Double rmsCh1;
    private Double peakToPeakCh1;
    private Double varianceCh1;
    private Double medianCh1;
    private Double percentile05Ch1;
    private Double percentile95Ch1;

    // --- CH2 ---
    private Double minCh2;
    private Double maxCh2;
    private Double meanCh2;
    private Double stdCh2;
    private Double rmsCh2;
    private Double peakToPeakCh2;
    private Double varianceCh2;
    private Double medianCh2;
    private Double percentile05Ch2;
    private Double percentile95Ch2;

    // --- CH3 ---
    private Double minCh3;
    private Double maxCh3;
    private Double meanCh3;
    private Double stdCh3;
    private Double rmsCh3;
    private Double peakToPeakCh3;
    private Double varianceCh3;
    private Double medianCh3;
    private Double percentile05Ch3;
    private Double percentile95Ch3;

    // --- OX ---
    private Double minOx;
    private Double maxOx;
    private Double meanOx;
    private Double stdOx;
    private Double rmsOx;
    private Double peakToPeakOx;
    private Double varianceOx;
    private Double medianOx;
    private Double percentile05Ox;
    private Double percentile95Ox;

    // --- OY ---
    private Double minOy;
    private Double maxOy;
    private Double meanOy;
    private Double stdOy;
    private Double rmsOy;
    private Double peakToPeakOy;
    private Double varianceOy;
    private Double medianOy;
    private Double percentile05Oy;
    private Double percentile95Oy;

    // --- OZ ---
    private Double minOz;
    private Double maxOz;
    private Double meanOz;
    private Double stdOz;
    private Double rmsOz;
    private Double peakToPeakOz;
    private Double varianceOz;
    private Double medianOz;
    private Double percentile05Oz;
    private Double percentile95Oz;
}
