/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.controller; 
 

import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.servis.SummonerProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/summoners")
@Tag(name = "Summoners")
public class SummonerProfileController {

    private final SummonerProfileService service;

    public SummonerProfileController(SummonerProfileService service) {
        this.service = service;
    }

    // GET /api/summoners
    @GetMapping
    public ResponseEntity<List<SummonerProfileDto>> all() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    // GET /api/summoners/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SummonerProfileDto> byId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET /api/summoners/search?name=Faker&region=EUNE
    @GetMapping("/search")
    public ResponseEntity<SummonerProfileDto> search(@RequestParam String name, @RequestParam Region region) {
        try {
            return ResponseEntity.ok(service.findByNameAndRegion(name, region));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // POST /api/summoners/upsert    (poziva front nakon Riot API poziva)
    @PostMapping("/upsert")
    public ResponseEntity<SummonerProfileDto> upsert(@RequestBody SummonerProfileDto dto) {
        return new ResponseEntity<>(service.upsert(dto), HttpStatus.OK);
    }

    // DELETE /api/summoners/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("Summoner deleted");
    }
}
