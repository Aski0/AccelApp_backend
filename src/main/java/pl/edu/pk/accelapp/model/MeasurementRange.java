package pl.edu.pk.accelapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "measurement_ranges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="range_start")
    private double start;
    @Column(name="range_end")
    private double end;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;

    // Możesz dodać datę utworzenia jeśli chcesz
    private LocalDateTime createdAt = LocalDateTime.now();

    // Opcjonalnie: nazwa zakresu nadana przez użytkownika
    private String name;

    // Ścieżka do obrazu PNG, jeśli zapisujesz wykres
    private String chartImagePath;
}

