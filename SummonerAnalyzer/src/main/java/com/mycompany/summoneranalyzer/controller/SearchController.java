package com.mycompany.summoneranalyzer.controller;

import com.mycompany.summoneranalyzer.dto.impl.SummonerMatchHistoryDto;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.servis.RiotMatchHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = {"http://localhost:3000"})
public class SearchController {

    private final RiotMatchHistoryService matchHistoryService;

    public SearchController(RiotMatchHistoryService matchHistoryService) {
        this.matchHistoryService = matchHistoryService;
    }

    @GetMapping
    public ResponseEntity<SummonerMatchHistoryDto> search(
        @RequestParam(required = false) String riotId,
        @RequestParam(required = false) String gameName,
        @RequestParam(required = false) String tagLine,
        @RequestParam(defaultValue = "EUNE") Region region,
        @RequestParam(defaultValue = "10") int count
    ) {
        try {
            SummonerMatchHistoryDto dto;
            if (riotId != null && !riotId.isBlank()) {
                dto = matchHistoryService.fetchByRiotId(riotId.trim(), region, count);
            } else if (gameName != null && !gameName.isBlank() && tagLine != null && !tagLine.isBlank()) {
                dto = matchHistoryService.fetchByRiotId(gameName.trim(), tagLine.trim(), region, count);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametar 'riotId' ili (gameName, tagLine) je obavezan");
            }
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
