package pl.edu.pk.accelapp.dto;

import lombok.Getter;
import lombok.Setter;
import pl.edu.pk.accelapp.model.Measurement;

@Getter
@Setter
public class MeasurementDto {
    private Long id;
    private Double time;
    private Double ox;
    private Double oy;
    private Double oz;
    private Double ch1;
    private Double ch2;
    private Double ch3;

    public MeasurementDto(Measurement m) {
        this.id = m.getId();
        this.time = m.getTime();
        this.ox = m.getOx();
        this.oy = m.getOy();
        this.oz = m.getOz();
        this.ch1 = m.getCh1();
        this.ch2 = m.getCh2();
        this.ch3 = m.getCh3();
    }
}
