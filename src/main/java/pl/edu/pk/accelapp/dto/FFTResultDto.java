package pl.edu.pk.accelapp.dto;

import java.util.List;
import java.util.Map;

public class FFTResultDto {

    private Map<String, List<Point>> data;

    public FFTResultDto(Map<String, List<Point>> data) {
        this.data = data;
    }

    public Map<String, List<Point>> getData() {
        return data;
    }

    public record Point(double freq, double magnitude) {}
}


