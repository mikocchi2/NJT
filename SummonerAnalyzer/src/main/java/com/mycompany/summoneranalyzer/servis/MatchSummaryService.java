/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis;
 

import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.entity.impl.Match;
import com.mycompany.summoneranalyzer.entity.impl.MatchSummary;
import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.mapper.impl.MatchSummaryMapper;
import com.mycompany.summoneranalyzer.repository.impl.MatchSummaryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors; 
import org.springframework.stereotype.Service;

@Service
public class MatchSummaryService {

    private final MatchSummaryRepository repo;
    private final MatchSummaryMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public MatchSummaryService(MatchSummaryRepository repo, MatchSummaryMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    public List<MatchSummaryDto> listForSummoner(Long summonerId, String gameType, Boolean win, Integer killsGte) {
        return repo.findByFilters(summonerId, gameType, win, killsGte)
                   .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MatchSummaryDto findById(Long id) throws Exception {
        return mapper.toDto(repo.findById(id));
    }

    @Transactional
    public MatchSummaryDto create(MatchSummaryDto dto) throws Exception {
        if (dto.getMatchId() == null || dto.getSummonerId() == null)
            throw new Exception("matchId and summonerId are required");

        MatchSummary e = new MatchSummary();
        e.setMatch(em.getReference(Match.class, dto.getMatchId()));
        e.setSummoner(em.getReference(SummonerProfile.class, dto.getSummonerId()));
        e.setChampion(dto.getChampion());
        e.setKills(dto.getKills() != null ? dto.getKills() : 0);
        e.setDeaths(dto.getDeaths() != null ? dto.getDeaths() : 0);
        e.setAssists(dto.getAssists() != null ? dto.getAssists() : 0);
        e.setWin(dto.getWin() != null ? dto.getWin() : false);

        repo.save(e);
        return mapper.toDto(e);
    }

    @Transactional
    public void deleteById(Long id) { repo.deleteById(id); }
    public List<MatchSummaryDto> findBySummonerId(Long summonerId) {
        return repo.findBySummonerId(summonerId).stream().map(mapper::toDto).toList();
    }
}
