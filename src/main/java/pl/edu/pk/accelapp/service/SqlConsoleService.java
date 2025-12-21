package pl.edu.pk.accelapp.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pk.accelapp.model.UploadedFile;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UploadedFileRepository;
import pl.edu.pk.accelapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

    @Service
    public class SqlConsoleService {

        private final JdbcTemplate jdbc;
        private final UserRepository userRepository;
        private final UploadedFileRepository uploadedFileRepository;

        public SqlConsoleService(JdbcTemplate jdbc,
                                 UserRepository userRepository,
                                 UploadedFileRepository uploadedFileRepository) {
            this.jdbc = jdbc;
            this.userRepository = userRepository;
            this.uploadedFileRepository = uploadedFileRepository;
        }

        public record QueryRequest(String sql, Integer limit, Integer timeoutSeconds) {}
        public sealed interface QueryResponse permits ResultSetResponse, UpdateCountResponse { String type(); }
        public record ResultSetResponse(String type, List<String> columns, List<List<Object>> rows) implements QueryResponse {}
        public record UpdateCountResponse(String type, int updated) implements QueryResponse {}

        @Transactional
        public QueryResponse runForFile(long fileId, QueryRequest req) {

            // 1) autoryzacja: czy ten fileId należy do zalogowanego usera
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UploadedFile file = uploadedFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            if (!file.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }

            // 2) walidacja SQL
            String sql = Objects.requireNonNullElse(req.sql(), "").trim();
            if (sql.isEmpty()) throw new IllegalArgumentException("Brak zapytania.");
            if (sql.contains(";")) throw new IllegalArgumentException("Jedno zapytanie na raz, bez średników.");

            String low = sql.toLowerCase(Locale.ROOT);
            if (!(low.startsWith("select") || low.startsWith("update") || low.startsWith("delete") || low.startsWith("insert"))) {
                throw new IllegalArgumentException("Dozwolone: SELECT / INSERT / UPDATE / DELETE");
            }

            int cap = Math.min(Objects.requireNonNullElse(req.limit(), 500), 2000);
            int timeoutMs = Math.min(Objects.requireNonNullElse(req.timeoutSeconds(), 5), 15) * 1000;

            // 3) wymuszenie roli + RLS context
            jdbc.execute("SET LOCAL ROLE app_console");

            jdbc.queryForObject(
                    "SELECT set_config('app.current_file_id', ?, true)",
                    String.class,
                    String.valueOf(fileId)
            );

            jdbc.execute("SET LOCAL statement_timeout = " + timeoutMs);

            // 4) wykonanie
            if (low.startsWith("select")) {
                String wrapped = ensureLimit(sql, cap);
                return runSelect(wrapped, cap);
            } else {
                int updated = jdbc.update(sql);
                return new UpdateCountResponse("updateCount", updated);
            }
        }

        private String ensureLimit(String sql, int limit) {
            String lowered = sql.toLowerCase(Locale.ROOT);
            boolean hasLimit = lowered.matches("(?s).*\\blimit\\s+\\d+.*");
            return hasLimit ? sql : sql + " LIMIT " + limit;
        }

        private ResultSetResponse runSelect(String sql, int cap) {
            SqlRowSet rs = jdbc.queryForRowSet(sql);
            var md = rs.getMetaData();
            int cols = md.getColumnCount();

            List<String> headers = new ArrayList<>(cols);
            for (int i = 1; i <= cols; i++) headers.add(md.getColumnLabel(i));

            List<List<Object>> rows = new ArrayList<>();
            int count = 0;
            while (rs.next() && count < cap) {
                List<Object> row = new ArrayList<>(cols);
                for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
                rows.add(row);
                count++;
            }
            return new ResultSetResponse("resultSet", headers, rows);
        }
    }
