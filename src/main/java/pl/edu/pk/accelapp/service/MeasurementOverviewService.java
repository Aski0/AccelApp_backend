package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.MeasurementRepository;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementOverviewService {

    private final MeasurementRepository measurementRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<OverviewBlock> getOverview(Long fileId, Authentication authentication, int blockSize) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        User currentUser = getCurrentUser(authentication);
        if (!file.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: file does not belong to user");
        }

        List<Measurement> measurements = measurementRepository.findByUploadedFileId(fileId);

        List<OverviewBlock> blocks = new ArrayList<>();
        int nBlocks = measurements.size() / blockSize;

        for (int i = 0; i < nBlocks; i++) {
            int start = i * blockSize;
            int end = start + blockSize;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            double sum = 0.0;

            for (int j = start; j < end; j++) {
                Measurement m = measurements.get(j);
                double mag = Math.sqrt(m.getOx()*m.getOx() + m.getOy()*m.getOy() + m.getOz()*m.getOz());
                if (mag < min) min = mag;
                if (mag > max) max = mag;
                sum += mag;
            }

            double mean = sum / blockSize;
            double timeSec = start / 9600.0;

            blocks.add(new OverviewBlock(timeSec, min, max, mean));
        }

        return blocks;
    }

    public static class OverviewBlock {
        private double timeSec;
        private double min;
        private double max;
        private double mean;

        public OverviewBlock(double timeSec, double min, double max, double mean) {
            this.timeSec = timeSec;
            this.min = min;
            this.max = max;
            this.mean = mean;
        }

        public double getTimeSec() { return timeSec; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getMean() { return mean; }
    }
}
