package pl.edu.pk.accelapp.dto;

import java.time.LocalDateTime;

public class FileHeaderDto {
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

    // gettery
    public Long getId() { return id; }
    public String getFilename() { return filename; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public long getMeasurementCount() { return measurementCount; }
}
