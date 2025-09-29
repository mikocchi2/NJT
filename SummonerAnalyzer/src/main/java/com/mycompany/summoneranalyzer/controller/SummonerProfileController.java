package com.mycompany.summoneranalyzer.controller;

import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.servis.SummonerProfileService;
import com.mycompany.summoneranalyzer.servis.SummonerSyncService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/summoners")
@Tag(name = "Summoners")
@CrossOrigin(origins = {  "http://localhost:3000" })
public class SummonerProfileController {

    private final SummonerProfileService profiles;
    private final SummonerSyncService sync;

    public SummonerProfileController(SummonerProfileService profiles, SummonerSyncService sync) {
        this.profiles = profiles;
        this.sync = sync;
    }

    @GetMapping
    public ResponseEntity<List<SummonerProfileDto>> all() {
        return ResponseEntity.ok(profiles.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SummonerProfileDto> byId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(profiles.findById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<SummonerProfileDto> search(
            @RequestParam String name,
            @RequestParam Region region
    ) {
        try {
            return ResponseEntity.ok(profiles.findByNameAndRegion(name, region));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** prima i 'name' i 'riotId' (front šalje 'riotId') */
    @PostMapping("/sync")
    public ResponseEntity<SummonerProfileDto> syncByName(
        @RequestParam(required = false) String name,
        @RequestParam(required = false, name = "riotId") String riotId,
        @RequestParam(defaultValue = "EUNE") Region region,
        @RequestParam(defaultValue = "10") int lastN
    ) {
        try {
            String input = (riotId != null && !riotId.isBlank()) ? riotId : name;
            if (input == null || input.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametar 'name' ili 'riotId' je obavezan");
            }
            return ResponseEntity.ok(sync.syncByName(input, region, lastN));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sync failed: " + e.getMessage());
        }
    }

    @PostMapping("/upsert")
    public ResponseEntity<SummonerProfileDto> upsert(@RequestBody SummonerProfileDto dto) {
        return ResponseEntity.ok(profiles.upsert(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        profiles.deleteById(id);
        return ResponseEntity.ok("Summoner deleted");
    }
}
