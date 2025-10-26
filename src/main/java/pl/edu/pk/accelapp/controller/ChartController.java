package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.dto.ChartDto;
import pl.edu.pk.accelapp.dto.RenameChartDto;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.ChartSnapshotRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartController {

    private final ChartSnapshotRepository chartRepo;
    private final UploadedFileRepository fileRepo;
    private final UserRepository userRepo;

    private User getCurrentUser(Principal principal) {
        return userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/file/{fileId}")
    public List<ChartDto> listForFile(@PathVariable Long fileId, Principal principal) {
        var user = getCurrentUser(principal);
        var file = fileRepo.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        if (!file.getUser().getId().equals(user.getId())) throw new RuntimeException("Access denied");

        return chartRepo.findByUploadedFileId(fileId).stream()
                .map(ChartDto::from)
                .toList();
    }

    @PatchMapping("/{chartId}")
    public ChartDto rename(@PathVariable Long chartId,
                           @RequestBody RenameChartDto body,
                           Principal principal) {
        var user = getCurrentUser(principal);
        var chart = chartRepo.findById(chartId).orElseThrow(() -> new RuntimeException("Chart not found"));
        if (!chart.getUploadedFile().getUser().getId().equals(user.getId())) throw new RuntimeException("Access denied");

        chart.setName(body.getName());
        var saved = chartRepo.save(chart);
        return ChartDto.from(saved);
    }

    @DeleteMapping("/{chartId}")
    public void delete(@PathVariable Long chartId, Principal principal) {
        var user = getCurrentUser(principal);
        var chart = chartRepo.findById(chartId).orElseThrow(() -> new RuntimeException("Chart not found"));
        if (!chart.getUploadedFile().getUser().getId().equals(user.getId())) throw new RuntimeException("Access denied");

        chartRepo.delete(chart);
    }
}
