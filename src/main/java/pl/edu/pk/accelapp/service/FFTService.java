package pl.edu.pk.accelapp.service;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pk.accelapp.dto.FFTResultDto;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.repository.MeasurementRepository;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FFTService {

    private final MeasurementRepository measurementRepository;

    private static final int WINDOW_SIZE = 8192;
    private static final double OVERLAP = 0.5;            // 50% nakładania
    private static final double SAMPLE_RATE_FAST = 4800.0;
    private static final double SAMPLE_RATE_SLOW = 10.0;
    private static final int MAX_POINTS = 5000;



    @Transactional(readOnly = true)
    public List<FFTResultDto.Point> computeFFT(Long fileId, String channel) {
        List<Double> signal = new ArrayList<>();

        // 🔹 wczytanie danych z bazy
        try (Stream<Measurement> stream = measurementRepository.streamByUploadedFileId(fileId)) {
            stream.forEach(m -> signal.add(getChannelValue(m, channel)));
        }

        if (signal.size() < WINDOW_SIZE) {
            return Collections.emptyList();
        }

        double sampleRate = switch (channel.toLowerCase()) {
            case "ch1", "ch2", "ch3" -> SAMPLE_RATE_FAST;
            default -> SAMPLE_RATE_SLOW;
        };

        // 🔹 obliczenie średniego widma (Catman-style)
        List<FFTResultDto.Point> averaged = computeAveragedFFT(signal, sampleRate);

        // 🔹 redukcja ilości punktów do wykresu
        return reducePoints(averaged);
    }

    private double getChannelValue(Measurement m, String channel) {
        return switch (channel.toLowerCase()) {
            case "ox" -> m.getOx();
            case "oy" -> m.getOy();
            case "oz" -> m.getOz();
            case "ch1" -> m.getCh1();
            case "ch2" -> m.getCh2();
            case "ch3" -> m.getCh3();
            default -> 0.0;
        };
    }

    /**
     * Catman Easy AP style FFT averaging (STFT z nakładaniem + okno Hanninga)
     */
    private List<FFTResultDto.Point> computeAveragedFFT(List<Double> signal, double sampleRate) {
        int hopSize = (int) (WINDOW_SIZE * (1.0 - OVERLAP)); // krok przesuwania okna
        int numWindows = (signal.size() - WINDOW_SIZE) / hopSize;

        double[] window = hanningWindow(WINDOW_SIZE);
        double[] avgMagnitude = new double[WINDOW_SIZE / 2];

        DoubleFFT_1D fft = new DoubleFFT_1D(WINDOW_SIZE);
        double[] buffer = new double[WINDOW_SIZE];

        // 🔹 przechodzimy po oknach
        for (int w = 0; w < numWindows; w++) {
            int offset = w * hopSize;
            for (int i = 0; i < WINDOW_SIZE; i++) {
                buffer[i] = signal.get(offset + i) * window[i];
            }

            fft.realForward(buffer);

            for (int i = 0; i < WINDOW_SIZE / 2; i++) {
                double re, im;
                if (i == 0) {
                    re = buffer[0];
                    im = 0.0;
                } else {
                    re = buffer[2 * i];
                    im = buffer[2 * i + 1];
                }

                double mag = Math.sqrt(re * re + im * im);

                // 🔹 NORMALIZACJA DO AMPLITUDY (peak)
                if (i == 0) {
                    // DC
                    mag = mag / WINDOW_SIZE;
                } else {
                    // pozostałe prążki – podwójna amplituda
                    mag = (2.0 * mag) / WINDOW_SIZE;
                }

                avgMagnitude[i] += mag;
            }
        }

// uśrednianie po oknach
        for (int i = 0; i < avgMagnitude.length; i++) {
            avgMagnitude[i] /= numWindows;
        }


        // 🔹 tworzenie listy punktów
        List<FFTResultDto.Point> result = new ArrayList<>(WINDOW_SIZE / 2);
        for (int i = 0; i < WINDOW_SIZE / 2; i++) {
            double freq = i * sampleRate / WINDOW_SIZE;
            result.add(new FFTResultDto.Point(freq, avgMagnitude[i]));
        }

        return result;
    }

    /**
     * Okno Hanninga (tłumienie przecieków widmowych)
     */
    private double[] hanningWindow(int size) {
        double[] w = new double[size];
        for (int i = 0; i < size; i++) {
            w[i] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / (size - 1));
        }
        return w;
    }

    /**
     * Redukcja liczby punktów (do wykresu)
     */
    private List<FFTResultDto.Point> reducePoints(List<FFTResultDto.Point> points) {
        if (points.size() <= MAX_POINTS) return points;
        int step = points.size() / MAX_POINTS;
        List<FFTResultDto.Point> reduced = new ArrayList<>(MAX_POINTS);
        for (int i = 0; i < points.size(); i += step) {
            reduced.add(points.get(i));
        }
        return reduced;
    }
}
