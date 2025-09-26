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


    private void bulkInsertMeasurements(MultipartFile multipartFile, Long uploadedFileId) throws Exception {
        File tempInput = File.createTempFile("upload-", ".txt");
        try (FileOutputStream fos = new FileOutputStream(tempInput)) {
            fos.write(multipartFile.getBytes());
        }

        File tempCsv = File.createTempFile("upload-csv-", ".csv");
        try (BufferedReader br = new BufferedReader(new FileReader(tempInput));
             FileWriter fw = new FileWriter(tempCsv)) {

            fw.write("time,ox,oy,oz,ch1,ch2,ch3,uploaded_file_id\n");

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] cols = line.split("\t");
                if (cols.length < 7) continue;

                fw.write(
                        cols[0].replace(",", ".") + "," +
                                cols[1].replace(",", ".") + "," +
                                cols[2].replace(",", ".") + "," +
                                cols[3].replace(",", ".") + "," +
                                cols[4].replace(",", ".") + "," +
                                cols[5].replace(",", ".") + "," +
                                cols[6].replace(",", ".") + "," +
                                uploadedFileId + "\n"
                );
            }
        }

        String sql = "COPY measurements(time, ox, oy, oz, ch1, ch2, ch3, uploaded_file_id) " +
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
