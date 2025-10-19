package pl.edu.pk.accelapp.dto;

public class ChartSnapshotDto {
    private Long fileId;
    private String name;
    private String type; // 'fft' lub 'range'
    private String base64Image;

    // getters & setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
}
