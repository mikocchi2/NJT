/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.controller; 
import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.servis.MatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List; 
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches")
public class MatchController {

    private final MatchService service;
    public MatchController(MatchService service) { this.service = service; }

    // GET /api/matches
    @GetMapping
    public ResponseEntity<List<MatchDto>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    // GET /api/matches/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> byId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // POST /api/matches/upsert
    @PostMapping("/upsert")
    public ResponseEntity<MatchDto> upsert(@RequestBody MatchDto dto) {
        return ResponseEntity.ok(service.upsert(dto));
    }

    // DELETE /api/matches/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok("Match deleted");
    }
}
