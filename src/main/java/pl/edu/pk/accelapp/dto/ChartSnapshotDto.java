package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.pk.accelapp.model.ChartSnapshot;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChartSnapshotDto {
    private Long id;           // id wykresu
    private String name;       // nazwa pliku
    private String type;       // 'fft' lub 'range'
    private String filePath;   // ścieżka do pliku na dysku
    private Long fileId;       // id pliku nadrzędnego
    private String base64Image; // opcjonalnie, tylko przy zapisie/odczycie Base64

    public static ChartSnapshotDto fromEntity(ChartSnapshot snapshot) {
        return new ChartSnapshotDto(
                snapshot.getId(),
                snapshot.getName(),
                snapshot.getType(),
                snapshot.getFilePath(),
                snapshot.getUploadedFile() != null ? snapshot.getUploadedFile().getId() : null,
                null // base64Image nie jest ustawiane przy pobieraniu listy
        );
    }
}
