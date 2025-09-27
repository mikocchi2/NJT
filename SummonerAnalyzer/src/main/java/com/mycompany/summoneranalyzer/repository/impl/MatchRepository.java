/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl;
 

import com.mycompany.summoneranalyzer.entity.impl.Match;
import com.mycompany.summoneranalyzer.repository.MyAppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List; 
import org.springframework.stereotype.Repository;

@Repository
public class MatchRepository implements MyAppRepository<Match, String> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Match> findAll() {
        return em.createQuery("SELECT m FROM Match m ORDER BY m.startedAt DESC", Match.class)
                 .getResultList();
    }

    @Override
    public Match findById(String id) throws Exception {
        Match m = em.find(Match.class, id);
        if (m == null) throw new Exception("Match not found: " + id);
        return m;
    }

    @Override @Transactional
    public void save(Match entity) {
        // ID je string (PK iz Riot-a) — koristimo merge za upsert
        em.merge(entity);
    }

    @Override @Transactional
    public void deleteById(String id) {
        Match m = em.find(Match.class, id);
        if (m != null) em.remove(m);
    }
}
