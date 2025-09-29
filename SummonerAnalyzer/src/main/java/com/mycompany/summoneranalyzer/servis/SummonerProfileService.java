/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis;
 

import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.mapper.impl.SummonerProfileMapper;
import com.mycompany.summoneranalyzer.repository.impl.SummonerProfileRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; 
import org.springframework.stereotype.Service;

@Service
public class SummonerProfileService {

    private final SummonerProfileRepository repo;
    private final SummonerProfileMapper mapper;

    public SummonerProfileService(SummonerProfileRepository repo, SummonerProfileMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    public List<SummonerProfileDto> findAll() {
        return repo.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public SummonerProfileDto findById(Long id) throws Exception {
        return mapper.toDto(repo.findById(id));
    }

    public SummonerProfileDto findByNameAndRegion(String name, Region region) throws Exception {
        SummonerProfile s = repo.findByNameAndRegion(name, region);
        if (s == null) throw new Exception("Summoner not found");
        return mapper.toDto(s);
    }

    /** Upsert iz Riot API podataka (ključ: puuid) */
    @Transactional
 
public SummonerProfileDto upsert(SummonerProfileDto dto) {
    SummonerProfile existing = dto.getPuuid() != null ? repo.findByPuuid(dto.getPuuid()) : null;
    SummonerProfile e = existing != null ? existing : new SummonerProfile();

    e.setPuuid(dto.getPuuid());

    // Fallback za name da NIKAD ne bude null/prazno (sprečava tvoj SQL constraint)
    String fallbackName = null;
    if (dto.getName() != null && !dto.getName().isBlank()) {
        fallbackName = dto.getName();
    } else if (dto.getPuuid() != null && dto.getPuuid().length() >= 8) {
        fallbackName = "Player-" + dto.getPuuid().substring(0, 8);
    } else {
        fallbackName = "Unknown";
    }
    e.setName(fallbackName);

    if (dto.getRegion() != null) e.setRegion(dto.getRegion());
    if (dto.getLevel() != null) e.setLevel(dto.getLevel());
    e.setRankTier(dto.getRankTier());
    e.setRankDivision(dto.getRankDivision());
    e.setLeaguePoints(dto.getLeaguePoints());
    e.setLastSyncedAt(LocalDateTime.now());

    repo.save(e);
    return mapper.toDto(e);
}


    @Transactional
    public void deleteById(Long id) { repo.deleteById(id); }
}
