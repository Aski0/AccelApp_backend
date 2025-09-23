package pl.edu.pk.accelapp.dto;

import lombok.Getter;
import lombok.Setter;
import pl.edu.pk.accelapp.repository.MeasurementRepository;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileHeaderDto {
    // gettery
    private Long id;
    private String filename;
    private LocalDateTime uploadedAt;
    private long measurementCount;

    public FileHeaderDto(Long id, String filename, LocalDateTime uploadedAt, long measurementCount) {
        this.id = id;
        this.filename = filename;
        this.uploadedAt = uploadedAt;
        this.measurementCount = measurementCount;
    }

}
