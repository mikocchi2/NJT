/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.controller; 

import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.servis.MatchSummaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List; 
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/match-summaries")
@Tag(name = "Match Summaries")
public class MatchSummaryController {

    private final MatchSummaryService service;
    public MatchSummaryController(MatchSummaryService service) { this.service = service; }

    // GET /api/match-summaries?summonerId=1&gameType=RANKED&win=true&killsGte=5
    @GetMapping
    public ResponseEntity<List<MatchSummaryDto>> list(
            @RequestParam Long summonerId,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) Boolean win,
            @RequestParam(required = false) Integer killsGte
    ) {
        return ResponseEntity.ok(service.listForSummoner(summonerId, gameType, win, killsGte));
    }

    // GET /api/match-summaries/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MatchSummaryDto> byId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // POST /api/match-summaries
    @PostMapping
    public ResponseEntity<MatchSummaryDto> create(@RequestBody MatchSummaryDto dto) {
        try {
            return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // DELETE /api/match-summaries/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("MatchSummary deleted");
    }
}
