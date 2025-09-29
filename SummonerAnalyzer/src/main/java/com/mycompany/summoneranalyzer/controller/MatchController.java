package com.mycompany.summoneranalyzer.controller;

import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.servis.MatchService;
import com.mycompany.summoneranalyzer.servis.MatchSummaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches")
public class MatchController {

    private final MatchService matches;
    private final MatchSummaryService summaries;

    public MatchController(MatchService matches, MatchSummaryService summaries) {
        this.matches = matches;
        this.summaries = summaries;
    }

    // GET /api/matches
    @GetMapping
    public ResponseEntity<List<MatchDto>> all() {
        return ResponseEntity.ok(matches.findAll());
    }

    // GET /api/matches/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MatchDto> byId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(matches.findById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // POST /api/matches/upsert
    @PostMapping("/upsert")
    public ResponseEntity<MatchDto> upsert(@RequestBody MatchDto dto) {
        return ResponseEntity.ok(matches.upsert(dto));
    }

    // DELETE /api/matches/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        matches.deleteById(id);
        return ResponseEntity.ok("Match deleted");
    }

    /**
     * GET /api/matches/by-summoner/{summonerId}
     * Query (opciono): gameType=Ranked|ARAM|Normal, win=true|false, minKills=5
     * Vraća listu plain JSON objekata sa poljima:
     * { matchId, champion, kills, deaths, assists, win, gameType, durationSec }
     */
    @GetMapping("/by-summoner/{summonerId}")
    public ResponseEntity<List<Map<String, Object>>> bySummoner(
            @PathVariable Long summonerId,
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) Boolean win,
            @RequestParam(required = false) Integer minKills
    ) throws Exception {
        // 1) summary-jevi za igrača
        List<MatchSummaryDto> list = summaries.findBySummonerId(summonerId);

        // 2) filteri
        if (win != null) {
            list = list.stream()
                    .filter(s -> Objects.equals(Boolean.TRUE.equals(s.getWin()), Boolean.TRUE.equals(win)))
                    .collect(Collectors.toList());
        }
        if (minKills != null && minKills > 0) {
            list = list.stream()
                    .filter(s -> s.getKills() != null && s.getKills() >= minKills)
                    .collect(Collectors.toList());
        }

        // 3) spoji sa Match (gameType, duration)
        List<Map<String, Object>> out = new ArrayList<>();
        for (MatchSummaryDto s : list) {
            MatchDto m = matches.findByIdOrNull(s.getMatchId());  

            String gtype = (m != null && m.getGameType() != null) ? m.getGameType().name() : null;
            Integer dur = (m != null) ? m.getDurationSec() : null;

            if (gameType != null && !gameType.isBlank()) {
                if (gtype == null || !gtype.equalsIgnoreCase(gameType)) continue;
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("matchId", s.getMatchId());
            row.put("champion", s.getChampion());
            row.put("kills", s.getKills());
            row.put("deaths", s.getDeaths());
            row.put("assists", s.getAssists());
            row.put("win", Boolean.TRUE.equals(s.getWin()));
            row.put("gameType", gtype);
            row.put("durationSec", dur);

            out.add(row);
        }

        return ResponseEntity.ok(out);
    }
}
