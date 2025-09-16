package pl.edu.pk.accelapp.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pk.accelapp.model.Measurement;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
//import javax.persistence.EntityNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FileUploadService {
    private final UploadedFileRepository uploadedFileRepository;
    private final MeasurementService measurementService;
    private final UserService userService;

    public FileUploadService(UploadedFileRepository uploadedFileRepository,
                             MeasurementService measurementService, UserService userService) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.measurementService = measurementService;
        this.userService = userService;
    }

    public UploadedFile saveFile(MultipartFile file, User user) throws IOException {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFileName(file.getOriginalFilename());
        uploadedFile.setUser(user);
        uploadedFile = uploadedFileRepository.save(uploadedFile);

        parseFileAndSaveMeasurements(file, uploadedFile);

        return uploadedFile;
    }

    private void parseFileAndSaveMeasurements(MultipartFile file, UploadedFile uploadedFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // pomijamy nagłówek
                    firstLine = false;
                    continue;
                }

                String[] values = line.split("\t");
                if (values.length < 7) continue;

                Measurement m = new Measurement();
                m.setTime(Double.parseDouble(values[0].replace(",", ".")));
                m.setOx(Double.parseDouble(values[1].replace(",", ".")));
                m.setOy(Double.parseDouble(values[2].replace(",", ".")));
                m.setOz(Double.parseDouble(values[3].replace(",", ".")));
                m.setCh1(Double.parseDouble(values[4].replace(",", ".")));
                m.setCh2(Double.parseDouble(values[5].replace(",", ".")));
                m.setCh3(Double.parseDouble(values[6].replace(",", ".")));
                m.setUploadedFile(uploadedFile);

                measurementService.saveMeasurement(m);
            }
        }
    }

    public UploadedFile saveFileForUser(MultipartFile file, Long userId) throws IOException {
        User user = userService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użytkownika o ID: " + userId));
        return saveFile(file, user);
    }
}
