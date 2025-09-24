package pl.edu.pk.accelapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "uploadedFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Measurement> measurement = new ArrayList<>();


//    @OneToOne(mappedBy = "uploadedFile",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true,
//            fetch = FetchType.LAZY)
//    @JsonIgnore
//    private TSTFile tstFile;
}

