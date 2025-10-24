package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.dto.ChartSnapshotDto;
import pl.edu.pk.accelapp.model.ChartSnapshot;
import pl.edu.pk.accelapp.repository.ChartSnapshotRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartCompareService {
    private final ChartSnapshotRepository chartSnapshotRepository;


    public List<ChartSnapshotDto> getDtosForUser(List<Long> ids, String userEmail) {
        List<ChartSnapshot> list = chartSnapshotRepository
                .findAllByIdInAndUserEmail(ids, userEmail);
        return list.stream().map(ChartSnapshotDto::fromEntity).collect(Collectors.toList());
    }


    public List<ImagePayload> getImagesBase64ForUser(List<Long> ids, String userEmail) {
        List<ChartSnapshot> list = chartSnapshotRepository
                .findAllByIdInAndUserEmail(ids, userEmail);
        return list.stream().map(cs -> {
            try {
                byte[] bytes = Files.readAllBytes(Path.of(cs.getFilePath()));
                String b64 = Base64.getEncoder().encodeToString(bytes);
                return new ImagePayload(cs.getId(), b64, cs.getName(), cs.getType());
            } catch (Exception e) {
                throw new RuntimeException("Nie udało się odczytać pliku: " + cs.getFilePath(), e);
            }
        }).collect(Collectors.toList());
    }


    public record ImagePayload(Long id, String base64Image, String name, String type) {}
}
