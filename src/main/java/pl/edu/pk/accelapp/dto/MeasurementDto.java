package pl.edu.pk.accelapp.dto;

import lombok.Getter;
import lombok.Setter;
import pl.edu.pk.accelapp.model.Measurement;

@Getter
@Setter
public class MeasurementDto {

    private Long id;
    private Double time;
    private Double ch1;
    private Double ch2;
    private Double ch3;
    private Double ch4;
    private Double ch5;
    private Double ch6;
    private Double ch7;
    private Double ch8;

    public MeasurementDto(Measurement m) {
        this.id = m.getId();
        this.time = m.getTime();
        this.ch1 = m.getCh1();
        this.ch2 = m.getCh2();
        this.ch3 = m.getCh3();
        this.ch4 = m.getCh4();
        this.ch5 = m.getCh5();
        this.ch6 = m.getCh6();
        this.ch7 = m.getCh7();
        this.ch8 = m.getCh8();
    }
}

