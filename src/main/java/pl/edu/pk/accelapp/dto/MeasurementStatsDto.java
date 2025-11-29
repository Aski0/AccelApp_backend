package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MeasurementStatsDto {

    private String filename;
    private long count;
    private List<ChannelStats> channels;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChannelStats {
        private String channel;   // np. "ch1"
        private Double min;
        private Double max;
        private Double mean;
        private Double stdDev;
        private Double rms;
        private Double peakToPeak;
        private Double variance;
        private Double median;
        private Double p05;
        private Double p95;
    }
}
