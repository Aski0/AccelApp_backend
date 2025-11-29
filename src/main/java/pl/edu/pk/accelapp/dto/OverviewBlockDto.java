package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OverviewBlockDto {
    private double timeSec;
    private double min;
    private double max;
    private double mean;
}