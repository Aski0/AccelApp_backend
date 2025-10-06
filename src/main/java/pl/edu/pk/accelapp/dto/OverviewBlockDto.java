package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OverviewBlockDto {
    private Long startIndex;  // indeks pierwszej próbki w bloku
    private Double min;
    private Double max;
    private Double mean;
    private Double std;
}
