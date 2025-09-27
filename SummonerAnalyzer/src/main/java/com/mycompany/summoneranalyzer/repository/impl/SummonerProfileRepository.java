/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl;
 

import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.repository.MyAppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List; 
import org.springframework.stereotype.Repository;

@Repository
public class SummonerProfileRepository implements MyAppRepository<SummonerProfile, Long> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<SummonerProfile> findAll() {
        return em.createQuery("SELECT s FROM SummonerProfile s ORDER BY s.name", SummonerProfile.class)
                 .getResultList();
    }

    @Override
    public SummonerProfile findById(Long id) throws Exception {
        SummonerProfile s = em.find(SummonerProfile.class, id);
        if (s == null) throw new Exception("Summoner not found: " + id);
        return s;
    }

    public SummonerProfile findByPuuid(String puuid) {
        List<SummonerProfile> list = em.createQuery(
                "SELECT s FROM SummonerProfile s WHERE s.puuid = :p", SummonerProfile.class)
                .setParameter("p", puuid)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public SummonerProfile findByNameAndRegion(String name, Region region) {
        List<SummonerProfile> list = em.createQuery(
            "SELECT s FROM SummonerProfile s WHERE lower(s.name)=lower(:n) AND s.region=:r", SummonerProfile.class)
            .setParameter("n", name)
            .setParameter("r", region)
            .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override @Transactional
    public void save(SummonerProfile entity) {
        if (entity.getId() == null) em.persist(entity);
        else em.merge(entity);
    }

    @Override @Transactional
    public void deleteById(Long id) {
        SummonerProfile s = em.find(SummonerProfile.class, id);
        if (s != null) em.remove(s);
    }
}
