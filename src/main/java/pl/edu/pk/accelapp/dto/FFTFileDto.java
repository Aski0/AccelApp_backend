package pl.edu.pk.accelapp.dto;

import java.util.List;

public class FFTFileDto {
    private List<FFTPointDto> ox;
    private List<FFTPointDto> oy;
    private List<FFTPointDto> oz;
    private List<FFTPointDto> ch1;
    private List<FFTPointDto> ch2;
    private List<FFTPointDto> ch3;

    public FFTFileDto(List<FFTPointDto> ox, List<FFTPointDto> oy, List<FFTPointDto> oz,
                      List<FFTPointDto> ch1, List<FFTPointDto> ch2, List<FFTPointDto> ch3) {
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.ch1 = ch1;
        this.ch2 = ch2;
        this.ch3 = ch3;
    }

    // Gettery
    public List<FFTPointDto> getOx() { return ox; }
    public List<FFTPointDto> getOy() { return oy; }
    public List<FFTPointDto> getOz() { return oz; }
    public List<FFTPointDto> getCh1() { return ch1; }
    public List<FFTPointDto> getCh2() { return ch2; }
    public List<FFTPointDto> getCh3() { return ch3; }
}

