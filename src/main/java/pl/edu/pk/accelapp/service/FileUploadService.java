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
import java.util.Locale;

@Service
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;
    private final DataSource dataSource;

    private static final int MAX_CHANNELS = 8;

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

        // surowy upload → tymczasowy plik
        File tempInput = File.createTempFile("upload-", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempInput)) {
            fos.write(multipartFile.getBytes());
        }

        File tempCsv = File.createTempFile("upload-csv-", ".csv");

        // wykrycie typu pliku po rozszerzeniu
        String originalName = multipartFile.getOriginalFilename();
        String lowerName = originalName != null ? originalName.toLowerCase(Locale.ROOT) : "";
        boolean isCsv = lowerName.endsWith(".csv");

        // TXT/ASC:  separator = TAB, liczby z przecinkiem (0,123)
        // CSV:      separator = ',', liczby już z kropką (0.123)
        String sep = isCsv ? "," : "\t";
        boolean decimalComma = !isCsv; // tylko dla txt/asc zamieniamy ',' -> '.'

        try (BufferedReader br = new BufferedReader(new FileReader(tempInput));
             FileWriter fw = new FileWriter(tempCsv)) {

            // nagłówek do COPY – stały
            fw.write("time,ch1,ch2,ch3,ch4,ch5,ch6,ch7,ch8,uploaded_file_id\n");

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // pierwsza linia – nagłówek pliku użytkownika → pomijamy
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // rozbij po separatorze
                String[] cols = line.split(sep, -1); // -1 → zachowuje puste kolumny
                if (cols.length < 1) {
                    continue; // musi być przynajmniej czas
                }

                StringBuilder sb = new StringBuilder();

                // TIME
                String timeRaw = cols[0].trim();
                String timeNorm = normalizeNumber(timeRaw, decimalComma);
                sb.append(timeNorm).append(",");

                // kanały: mapujemy kolejne kolumny na ch1..ch8
                // dla CSV z przykładu:
                //  time,ox,oy,oz
                //  → ch1 = ox, ch2 = oy, ch3 = oz
                int available = Math.min(MAX_CHANNELS, cols.length - 1);
                for (int i = 0; i < MAX_CHANNELS; i++) {
                    if (i < available) {
                        String raw = cols[1 + i].trim();
                        String norm = normalizeNumber(raw, decimalComma);
                        sb.append(norm);
                    }
                    sb.append(",");
                }

                // uploaded_file_id
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

    /**
     * Normalizuje zapis liczby:
     *  - jeśli decimalComma == true, zamienia przecinek na kropkę
     *  - jeśli pusty string → zostawiamy pusty (NULL w COPY)
     */
    private String normalizeNumber(String raw, boolean decimalComma) {
        if (raw == null) return "";
        raw = raw.trim();
        if (raw.isEmpty()) return "";
        if (decimalComma) {
            return raw.replace(",", ".");
        }
        return raw;
    }
}
