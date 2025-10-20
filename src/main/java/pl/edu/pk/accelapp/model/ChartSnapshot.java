package pl.edu.pk.accelapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chart_snapshots")
public class ChartSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // np. FFT_OX_2025-10-18.png

    private String type; // 'fft' lub 'range'

    private String filePath; // ścieżka na dysku

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "file_id")
    private UploadedFile uploadedFile;

}
