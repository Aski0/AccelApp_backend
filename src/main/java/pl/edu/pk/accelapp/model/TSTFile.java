package pl.edu.pk.accelapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tst_files")
public class TSTFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tstFilename;

    @Lob
    private String content;

    @OneToOne
    @JoinColumn(name = "uploaded_file_id", referencedColumnName = "id", unique = true)
    private UploadedFile uploadedFile;
}
