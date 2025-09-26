package pl.edu.pk.accelapp.service;

import lombok.RequiredArgsConstructor;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pk.accelapp.dto.FFTPointDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.repository.MeasurementRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FFTService {

    private final MeasurementRepository measurementRepository;

    private static final int CHUNK_SIZE = 1024;
    private static final double SAMPLE_RATE_FAST = 4800.0;
    private static final double SAMPLE_RATE_SLOW = 10.0;


    @Transactional(readOnly = true)
    public List<FFTPointDto> computeFFT(Long fileId, String channel) {
        List<FFTPointDto> fftResult = new ArrayList<>();

        try (Stream<Measurement> stream = measurementRepository.streamByUploadedFileId(fileId)) {
            List<Double> buffer = new ArrayList<>(CHUNK_SIZE);

            stream.forEach(m -> {
                double value = getChannelValue(m, channel);
                buffer.add(value);

                if (buffer.size() == CHUNK_SIZE) {
                    fftResult.addAll(computeFFTChunk(buffer, channel));
                    buffer.clear();
                }
            });
            if (!buffer.isEmpty()) {
                fftResult.addAll(computeFFTChunk(buffer, channel));
            }
        }

        return fftResult;
    }

    private double getChannelValue(Measurement m, String channel) {
        return switch (channel.toLowerCase()) {
            case "ox" -> m.getOx();
            case "oy" -> m.getOy();
            case "oz" -> m.getOz();
            case "ch1" -> m.getCh1();
            case "ch2" -> m.getCh2();
            case "ch3" -> m.getCh3();
            default -> 0;
        };
    }

    private List<FFTPointDto> computeFFTChunk(List<Double> chunk, String channel) {
        int n = chunk.size();
        double[] data = new double[n];
        for (int i = 0; i < n; i++) data[i] = chunk.get(i);

        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        fft.realForward(data);

        List<FFTPointDto> result = new ArrayList<>(n / 2);
        double sampleRate = channel.equalsIgnoreCase("ch1") ||
                channel.equalsIgnoreCase("ch2") ||
                channel.equalsIgnoreCase("ch3")
                ? SAMPLE_RATE_FAST : SAMPLE_RATE_SLOW;

        for (int i = 0; i < n / 2; i++) {
            double freq = i * sampleRate / n;
            double magnitude = Math.abs(data[i]);
            result.add(new FFTPointDto(freq, magnitude));
        }

        return result;
    }
}
