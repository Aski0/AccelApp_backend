package pl.edu.pk.accelapp.service;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;

import javax.sql.DataSource;
import java.io.*;
import java.time.LocalDateTime;

@Service
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;
    private final DataSource dataSource;

    public FileUploadService(UploadedFileRepository uploadedFileRepository,
                             DataSource dataSource) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.dataSource = dataSource;
    }

    public void saveFile(MultipartFile multipartFile, User user) throws Exception {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename(multipartFile.getOriginalFilename());
        uploadedFile.setUploadedAt(LocalDateTime.now());
        uploadedFile.setUser(user);
        uploadedFile = uploadedFileRepository.save(uploadedFile);

        bulkInsertMeasurements(multipartFile, uploadedFile.getId());
    }

    private static final int MAX_CHANNELS = 8;

    private void bulkInsertMeasurements(MultipartFile multipartFile, Long uploadedFileId) throws Exception {

        File tempInput = File.createTempFile("upload-", ".txt");
        try (FileOutputStream fos = new FileOutputStream(tempInput)) {
            fos.write(multipartFile.getBytes());
        }

        File tempCsv = File.createTempFile("upload-csv-", ".csv");

        try (BufferedReader br = new BufferedReader(new FileReader(tempInput));
             FileWriter fw = new FileWriter(tempCsv)) {

            // Generujemy nagłówek time + 8 kanałów
            fw.write("time,ch1,ch2,ch3,ch4,ch5,ch6,ch7,ch8,uploaded_file_id\n");

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split("\t");
                if (cols.length < 1) continue; // musi być chociaż time

                StringBuilder sb = new StringBuilder();

                // TIME
                sb.append(cols[0].replace(",", ".")).append(",");

                // kanały (od cols[1] do cols[x])
                int available = Math.min(MAX_CHANNELS, cols.length - 1);

                for (int i = 0; i < MAX_CHANNELS; i++) {
                    if (i < available) {
                        sb.append(cols[1 + i].replace(",", "."));
                    }
                    sb.append(",");
                }

                sb.append(uploadedFileId).append("\n");
                fw.write(sb.toString());
            }
        }

        String sql =
                "COPY measurements(time, ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8, uploaded_file_id) " +
                        "FROM STDIN WITH (FORMAT csv, HEADER true, DELIMITER ',')";

        try (var connection = dataSource.getConnection();
             var reader = new FileReader(tempCsv)) {

            CopyManager copyManager = new CopyManager(connection.unwrap(BaseConnection.class));
            copyManager.copyIn(sql, reader);
        }

        tempInput.delete();
        tempCsv.delete();
    }
}
