package pl.edu.pk.accelapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private LocalDateTime uploadedAt;

    private Long duration;

    // typ pliku: np. "DATA" albo "TST"
    private String fileType;

    // jeśli to jest plik TST → wskazuje na plik pomiarowy
    @ManyToOne
    @JoinColumn(name = "measurement_file_id")
    private UploadedFile measurementFile;

    // jeśli to jest plik DATA → może mieć przypisany plik TST
    @OneToOne(mappedBy = "measurementFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UploadedFile tstFile;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "uploadedFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Measurement> measurement = new ArrayList<>();

    public void setFileName(String filename) {
        this.filename = filename;
    }
}
