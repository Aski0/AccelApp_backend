package pl.edu.pk.accelapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "measurements")
public class Measurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double time;
    private Double ox;
    private Double oy;
    private Double oz;
    private Double ch1;
    private Double ch2;
    private Double ch3;

    @ManyToOne
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;
}
