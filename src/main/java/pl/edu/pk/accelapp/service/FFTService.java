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
    private static final double SAMPLE_RATE_FAST = 9600.0;
    private static final double SAMPLE_RATE_SLOW = 10.0;
    private static final int MAX_POINTS = 5000;

    @Transactional(readOnly = true)
    public List<FFTResultDto.Point> computeFFT(Long fileId, String channel) {
        if (channel == null || channel.isBlank()) {
            return Collections.emptyList();
        }

        String normalizedChannel = normalizeChannelName(channel);

        List<Double> signal = new ArrayList<>();
        try (Stream<Measurement> stream = measurementRepository.streamByUploadedFileId(fileId)) {
            stream.forEach(m -> signal.add(getChannelValue(m, normalizedChannel)));
        }

        if (signal.size() < WINDOW_SIZE) {
            return Collections.emptyList();
        }

        double sampleRate = switch (normalizedChannel) {
            case "ch1", "ch2", "ch3" -> SAMPLE_RATE_FAST;
            case "ch4", "ch5", "ch6", "ch7", "ch8" -> SAMPLE_RATE_SLOW;
            default -> SAMPLE_RATE_SLOW;
        };

        // 🔹 obliczenie średniego widma (Catman-style)
        List<FFTResultDto.Point> averaged = computeAveragedFFT(signal, sampleRate);

        // 🔹 redukcja ilości punktów do wykresu
        return reducePoints(averaged);
    }

    /**
     * Normalizacja nazwy kanału:
     * "ch1" / "CH1" / "1" -> "ch1"
     */
    private String normalizeChannelName(String channel) {
        String ch = channel.trim().toLowerCase();
        if (!ch.startsWith("ch")) {
            ch = "ch" + ch;
        }
        return ch;
    }

    private double getChannelValue(Measurement m, String channel) {
        return switch (channel) {
            case "ch1" -> safe(m.getCh1());
            case "ch2" -> safe(m.getCh2());
            case "ch3" -> safe(m.getCh3());
            case "ch4" -> safe(m.getCh4());
            case "ch5" -> safe(m.getCh5());
            case "ch6" -> safe(m.getCh6());
            case "ch7" -> safe(m.getCh7());
            case "ch8" -> safe(m.getCh8());
            default -> 0.0;
        };
    }

    private double safe(Double v) {
        return v != null ? v : 0.0;
    }

    /**
     * Catman Easy AP style FFT averaging (STFT z nakładaniem + okno Hanninga)
     */
    /**
     * FFT jak w diagnostyce drgań (Catman-style):
     * - FFT 8192
     * - Overlap 50%
     * - Okno Hamminga
     * - Linear averaging
     * - Widmo amplitudowe RMS
     */
    private List<FFTResultDto.Point> computeAveragedFFT(List<Double> signal, double sampleRate) {

        int hopSize = WINDOW_SIZE / 2; // 50% overlap

        if (signal.size() < WINDOW_SIZE) {
            return Collections.emptyList();
        }

        int numWindows = 1 + (signal.size() - WINDOW_SIZE) / hopSize;
        if (numWindows <= 0) {
            return Collections.emptyList();
        }

        double[] window = hammingWindow(WINDOW_SIZE);
        double[] avgMagnitude = new double[WINDOW_SIZE / 2];

        DoubleFFT_1D fft = new DoubleFFT_1D(WINDOW_SIZE);
        double[] buffer = new double[WINDOW_SIZE];

        for (int w = 0; w < numWindows; w++) {
            int offset = w * hopSize;

            // 🔹 okno czasowe
            for (int i = 0; i < WINDOW_SIZE; i++) {
                buffer[i] = signal.get(offset + i) * window[i];
            }

            // 🔹 FFT
            fft.realForward(buffer);

            // 🔹 widmo jednostronne
            for (int i = 0; i < WINDOW_SIZE / 2; i++) {

                double re, im;
                if (i == 0) {           // DC
                    re = buffer[0];
                    im = 0.0;
                } else {
                    re = buffer[2 * i];
                    im = buffer[2 * i + 1];
                }

                double mag = Math.sqrt(re * re + im * im);

                // 🔹 normalizacja amplitudy (peak)
                if (i == 0) {
                    mag = mag / WINDOW_SIZE;
                } else {
                    mag = (2.0 * mag) / WINDOW_SIZE;
                }

                // 🔹 Peak → RMS (Catman)
                mag /= Math.sqrt(2.0);

                avgMagnitude[i] += mag;
            }
        }

        // 🔹 uśrednianie liniowe
        for (int i = 0; i < avgMagnitude.length; i++) {
            avgMagnitude[i] /= numWindows;
        }

        // 🔹 wynik (częstotliwość + amplituda RMS)
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
    private double[] hammingWindow(int size) {
        double[] w = new double[size];
        for (int i = 0; i < size; i++) {
            w[i] = 0.54 - 0.46 * Math.cos(2.0 * Math.PI * i / (size - 1));
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
