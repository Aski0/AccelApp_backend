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

    /**
     * Zapisuje plik i wykonuje bulk load do PostgreSQL
     */
    public void saveFile(MultipartFile multipartFile, User user) throws Exception {
        // 1️⃣ Zapisz metadane pliku w bazie
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFilename(multipartFile.getOriginalFilename());
        uploadedFile.setUploadedAt(LocalDateTime.now());
        uploadedFile.setUser(user);
        uploadedFile = uploadedFileRepository.save(uploadedFile);

        // 2️⃣ Bulk import pliku do tabeli measurement
        bulkInsertMeasurements(multipartFile, uploadedFile.getId());
    }

    /**
     * Bulk load CSV do PostgreSQL z konwersją tabów na przecinki i przecinka dziesiętnego na kropkę
     */
    private void bulkInsertMeasurements(MultipartFile multipartFile, Long uploadedFileId) throws Exception {
        // Zapisz plik tymczasowo
        File tempInput = File.createTempFile("upload-", ".txt");
        try (FileOutputStream fos = new FileOutputStream(tempInput)) {
            fos.write(multipartFile.getBytes());
        }

        // Utwórz nowy plik CSV zgodny z PostgreSQL
        File tempCsv = File.createTempFile("upload-csv-", ".csv");
        try (BufferedReader br = new BufferedReader(new FileReader(tempInput));
             FileWriter fw = new FileWriter(tempCsv)) {

            // Nagłówek w formacie PostgreSQL
            fw.write("time,ox,oy,oz,ch1,ch2,ch3,uploaded_file_id\n");

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // pomiń stary nagłówek
                    firstLine = false;
                    continue;
                }

                // Zamiana tabów na przecinek i przecinka dziesiętnego na kropkę
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

        // COPY do PostgreSQL
        String sql = "COPY measurements(time, ox, oy, oz, ch1, ch2, ch3, uploaded_file_id) " +
                "FROM STDIN WITH (FORMAT csv, HEADER true, DELIMITER ',')";

        try (var connection = dataSource.getConnection();
             var reader = new FileReader(tempCsv)) {

            CopyManager copyManager = new CopyManager(connection.unwrap(BaseConnection.class));
            copyManager.copyIn(sql, reader);
        }

        // Usuń pliki tymczasowe
        tempInput.delete();
        tempCsv.delete();
    }
}
