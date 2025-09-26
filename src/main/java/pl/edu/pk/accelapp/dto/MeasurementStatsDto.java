package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MeasurementStatsDto {
    private Long count;

    private Double minCh1;
    private Double maxCh1;
    private Double meanCh1;
    private Double stdCh1;

    private Double minCh2;
    private Double maxCh2;
    private Double meanCh2;
    private Double stdCh2;

    private Double minCh3;
    private Double maxCh3;
    private Double meanCh3;
    private Double stdCh3;

    private Double minOx;
    private Double maxOx;
    private Double meanOx;
    private Double stdOx;

    private Double minOy;
    private Double maxOy;
    private Double meanOy;
    private Double stdOy;

    private Double minOz;
    private Double maxOz;
    private Double meanOz;
    private Double stdOz;
}
