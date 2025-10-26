package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.pk.accelapp.model.ChartSnapshot;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
public class ChartDto {
    private Long id;
    private String name;
    private String type;
    private LocalDateTime createdAt;

    public static ChartDto from(ChartSnapshot cs) {
        return new ChartDto(cs.getId(), cs.getName(), cs.getType(), cs.getCreatedAt());
    }
}
