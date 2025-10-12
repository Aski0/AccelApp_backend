package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.MeasurementRange;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.MeasurementRangeRepository;
import pl.edu.pk.accelapp.repository.MeasurementRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementRangeService {

    private final MeasurementRepository measurementRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final MeasurementRangeRepository rangeRepository;
    private final UserRepository userRepository;

    // 🔒 pomocnicza metoda do autoryzacji
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 📊 Pobieranie pomiarów w zakresie
    public List<Measurement> getMeasurementsInRange(Long fileId, Authentication authentication, double startSec, double endSec) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User user = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied: file does not belong to user");

        return measurementRepository.findByUploadedFileId(fileId).stream()
                .filter(m -> m.getTime() >= startSec && m.getTime() <= endSec)
                .toList();
    }

    // 📋 Lista zakresów dla pliku
    public List<MeasurementRange> getRangesForFile(Long fileId, Authentication authentication) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User user = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied: file does not belong to user");

        return rangeRepository.findByUploadedFileId(fileId);
    }

    // 💾 Zapis nowego zakresu
    public MeasurementRange saveRange(Long fileId, double start, double end, String chartPath, Authentication authentication) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User user = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied: file does not belong to user");

        MeasurementRange range = new MeasurementRange();
        range.setUploadedFile(file);
        range.setStart(start);
        range.setEnd(end);
        range.setChartImagePath(chartPath);

        return rangeRepository.save(range);
    }

    // ❌ Usuń istniejący zakres
    public void deleteRange(Long rangeId, Authentication authentication) {
        MeasurementRange range = rangeRepository.findById(rangeId)
                .orElseThrow(() -> new RuntimeException("Range not found"));

        User user = getCurrentUser(authentication);
        if (!range.getUploadedFile().getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied: range does not belong to user");

        rangeRepository.delete(range);
    }
}
