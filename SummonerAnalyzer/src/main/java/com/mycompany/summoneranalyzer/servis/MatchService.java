/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis; 

import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.entity.impl.Match;
import com.mycompany.summoneranalyzer.mapper.impl.MatchMapper;
import com.mycompany.summoneranalyzer.repository.impl.MatchRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors; 
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final MatchRepository repo;
    private final MatchMapper mapper;

    public MatchService(MatchRepository repo, MatchMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    public List<MatchDto> findAll() {
        return repo.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MatchDto findById(String id) throws Exception {
        return mapper.toDto(repo.findById(id));
    }

    @Transactional
    public MatchDto upsert(MatchDto dto) {
        Match e = mapper.toEntity(dto);
        repo.save(e);
        return mapper.toDto(e);
    }

    @Transactional
    public void deleteById(String id) { repo.deleteById(id); }
}

