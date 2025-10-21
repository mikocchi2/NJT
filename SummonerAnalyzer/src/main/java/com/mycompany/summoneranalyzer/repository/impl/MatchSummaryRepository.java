/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl; 

import com.mycompany.summoneranalyzer.entity.impl.MatchSummary;
import com.mycompany.summoneranalyzer.entity.impl.enums.GameType;
import com.mycompany.summoneranalyzer.repository.MyAppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List; 
import org.springframework.stereotype.Repository;

@Repository
public class MatchSummaryRepository implements MyAppRepository<MatchSummary, Long> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<MatchSummary> findAll() {
        return em.createQuery(
            "SELECT ms FROM MatchSummary ms ORDER BY ms.id DESC", MatchSummary.class)
            .getResultList();
    }

    @Override
    public MatchSummary findById(Long id) throws Exception {
        MatchSummary ms = em.find(MatchSummary.class, id);
        if (ms == null) throw new Exception("MatchSummary not found: " + id);
        return ms;
    }

    /** Lista mečeva za summoner-a uz opcione filtere */
    public List<MatchSummary> findByFilters(Long summonerId, String gameType, Boolean win, Integer killsGte) {
        String jpql =
            "SELECT ms FROM MatchSummary ms " +
            "JOIN ms.match m " +
            "WHERE ms.summoner.id = :sid " +
            (gameType != null ? "AND m.gameType = :gt " : "") +
            (win != null ? "AND ms.win = :w " : "") +
            (killsGte != null ? "AND ms.kills >= :kg " : "") +
            "ORDER BY m.startedAt DESC";

        var q = em.createQuery(jpql, MatchSummary.class)
                  .setParameter("sid", summonerId);
        if (gameType != null) q.setParameter("gt", Enum.valueOf( GameType.class, gameType));
        if (win != null) q.setParameter("w", win);
        if (killsGte != null) q.setParameter("kg", killsGte);
        return q.getResultList();
    }

    @Override @Transactional
    public void save(MatchSummary entity) {
        if (entity.getId() == null) em.persist(entity);
        else em.merge(entity);
    }

    @Override @Transactional
    public void deleteById(Long id) {
        MatchSummary ms = em.find(MatchSummary.class, id);
        if (ms != null) em.remove(ms);
    }

    public MatchSummary findByMatchAndSummoner(String matchId, Long summonerId) {
        return em.createQuery(
                "SELECT ms FROM MatchSummary ms " +
                "WHERE ms.match.id = :mid AND ms.summoner.id = :sid",
                MatchSummary.class)
            .setParameter("mid", matchId)
            .setParameter("sid", summonerId)
            .getResultStream()
            .findFirst()
            .orElse(null);
    }

    public List<MatchSummary> findBySummonerId(Long summonerId) {
        return em.createQuery(
                "SELECT ms FROM MatchSummary ms " +
                "JOIN FETCH ms.match m " +            // da odmah povučemo match (gameType/duration)
                "WHERE ms.summoner.id = :sid " +
                "ORDER BY m.startedAt DESC", MatchSummary.class)
            .setParameter("sid", summonerId)
            .getResultList();
    }
}
