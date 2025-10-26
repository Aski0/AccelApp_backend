package pl.edu.pk.accelapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.edu.pk.accelapp.service.SqlConsoleService;

@RestController
@RequestMapping("/api/files/{fileId}/console")
@RequiredArgsConstructor
public class SqlConsoleController {

    private final SqlConsoleService service;

    @PostMapping
    public SqlConsoleService.QueryResponse run(
            @PathVariable long fileId,
            @RequestBody SqlConsoleService.QueryRequest req
    ) {
        return service.runForFile(fileId, req);
    }
}
