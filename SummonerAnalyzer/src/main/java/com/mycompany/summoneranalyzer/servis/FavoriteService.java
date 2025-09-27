/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis;
 

import com.mycompany.summoneranalyzer.dto.impl.FavoriteDto;
import com.mycompany.summoneranalyzer.entity.impl.Favorite;
import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.entity.impl.User;
import com.mycompany.summoneranalyzer.mapper.impl.FavoriteMapper;
import com.mycompany.summoneranalyzer.repository.impl.FavoriteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors; 
import org.springframework.stereotype.Service;

@Service
public class FavoriteService {

    private final FavoriteRepository repo;
    private final FavoriteMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public FavoriteService(FavoriteRepository repo, FavoriteMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    public List<FavoriteDto> listByUser(Long userId) {
        return repo.findAllByUserId(userId).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public FavoriteDto add(FavoriteDto dto) throws Exception {
        if (dto.getUserId() == null || dto.getSummonerId() == null)
            throw new Exception("userId and summonerId are required");

        // zabrani duplikat
        if (repo.findByUserIdAndSummonerId(dto.getUserId(), dto.getSummonerId()).isPresent())
            throw new Exception("Already in favorites");

        Favorite f = new Favorite();
        f.setUser(em.getReference(User.class, dto.getUserId()));
        f.setSummoner(em.getReference(SummonerProfile.class, dto.getSummonerId()));
        f.setNote(dto.getNote());

        repo.save(f);
        return mapper.toDto(f);
    }

    @Transactional
    public FavoriteDto updateNote(Long id, String note) throws Exception {
        Favorite f = repo.findById(id);
        f.setNote(note);
        repo.save(f);
        return mapper.toDto(f);
    }

    @Transactional
    public void remove(Long id) { repo.deleteById(id); }
}
