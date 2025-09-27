/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.controller; 

import com.mycompany.summoneranalyzer.dto.impl.FavoriteDto;
import com.mycompany.summoneranalyzer.servis.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List; 
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites")
public class FavoriteController {

    private final FavoriteService service;
    public FavoriteController(FavoriteService service) { this.service = service; }

    // GET /api/favorites/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteDto>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }

    // POST /api/favorites
    @PostMapping
    public ResponseEntity<FavoriteDto> add(@RequestBody FavoriteDto dto) {
        try {
            return new ResponseEntity<>(service.add(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // PATCH /api/favorites/{id}/note?value=...
    @PatchMapping("/{id}/note")
    public ResponseEntity<FavoriteDto> updateNote(@PathVariable Long id, @RequestParam String value) {
        try {
            return ResponseEntity.ok(service.updateNote(id, value));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // DELETE /api/favorites/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> remove(@PathVariable Long id) {
        service.remove(id);
        return ResponseEntity.ok("Favorite removed");
    }
}
