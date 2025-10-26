package pl.edu.pk.accelapp.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class SqlConsoleService {

    private final JdbcTemplate jdbc;

    public SqlConsoleService(JdbcTemplate jdbc) { // domyślny, z jednego DS
        this.jdbc = jdbc;
    }

    // ====== DTO ======
    public record QueryRequest(String sql, Integer limit, Integer timeoutSeconds) {}
    public sealed interface QueryResponse permits ResultSetResponse, UpdateCountResponse {
        String type();
    }
    public record ResultSetResponse(String type, List<String> columns, List<List<Object>> rows)
            implements QueryResponse {}
    public record UpdateCountResponse(String type, int updated)
            implements QueryResponse {}

    // ====== Główna metoda ======
    @Transactional
    public QueryResponse runForFile(long fileId, QueryRequest req) {
        String sql = Objects.requireNonNullElse(req.sql(), "").trim();
        if (sql.isEmpty()) throw new IllegalArgumentException("Brak zapytania.");
        if (sql.contains(";")) throw new IllegalArgumentException("Jedno zapytanie na raz, bez średników.");

        String low = sql.toLowerCase(Locale.ROOT);
        if (!(low.startsWith("select") || low.startsWith("update") || low.startsWith("delete") || low.startsWith("insert"))) {
            throw new IllegalArgumentException("Dozwolone: SELECT / INSERT / UPDATE / DELETE");
        }

        int cap = Math.min(Objects.requireNonNullElse(req.limit(), 500), 2000);
        int timeoutMs = Math.min(Objects.requireNonNullElse(req.timeoutSeconds(), 5), 15) * 1000;

        // 🔥 WYMUSZENIE RLS nawet dla root/superuser:
        jdbc.execute("SET LOCAL ROLE app_console");

        // 🔒 OGRANICZENIE fileId (RLS)
        jdbc.queryForObject(
                "SELECT set_config('app.current_file_id', ?, true)",
                String.class,
                String.valueOf(fileId)
        );

        // ⏳ Limit czasu
        jdbc.execute("SET LOCAL statement_timeout = " + timeoutMs);

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
