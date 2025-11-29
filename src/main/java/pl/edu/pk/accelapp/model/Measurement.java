package pl.edu.pk.accelapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "measurements")
@Getter
@Setter
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;
}
