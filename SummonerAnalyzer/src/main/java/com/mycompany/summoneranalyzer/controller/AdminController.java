 
package com.mycompany.summoneranalyzer.controller; 

import com.mycompany.summoneranalyzer.dto.impl.ApiCallLogDto;
import com.mycompany.summoneranalyzer.entity.impl.User;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.servis.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List; 
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<List<User>> users() {
        return ResponseEntity.ok(service.listUsers());
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.ok("User deleted");
    }

    // GET /api/admin/logs?status=200&region=EUNE&from=2025-09-01T00:00:00&to=2025-09-30T23:59:59
    @GetMapping("/logs")
    public ResponseEntity<List<ApiCallLogDto>> logs(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(service.listLogs(status, region, from, to));
    }

    // POST /api/admin/logs
    @PostMapping("/logs")
    public ResponseEntity<ApiCallLogDto> append(@RequestBody ApiCallLogDto dto) {
        return new ResponseEntity<>(service.append(dto), HttpStatus.CREATED);
    }

    // DELETE /api/admin/logs/{id}
    @DeleteMapping("/logs/{id}")
    public ResponseEntity<String> deleteLog(@PathVariable Long id) {
        service.deleteLog(id);
        return ResponseEntity.ok("Log deleted");
    }
}
