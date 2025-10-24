package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pk.accelapp.dto.ChartSnapshotDto;
import pl.edu.pk.accelapp.service.ChartCompareService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartCompareController {
    private final ChartCompareService chartCompareService;


    @GetMapping("/compare")
    public ResponseEntity<List<ChartSnapshotDto>> compare(@RequestParam("ids") String ids,
                                                          Authentication auth) {
        String email = auth.getName();
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .limit(3)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chartCompareService.getDtosForUser(idList, email));
    }


    // Opcjonalny endpoint na 1 strzał z obrazami Base64
    @GetMapping("/compare/images")
    public ResponseEntity<List<?>> compareImages(@RequestParam("ids") String ids,
                                                 Authentication auth) {
        String email = auth.getName();
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .limit(3)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chartCompareService.getImagesBase64ForUser(idList, email));
    }
}
