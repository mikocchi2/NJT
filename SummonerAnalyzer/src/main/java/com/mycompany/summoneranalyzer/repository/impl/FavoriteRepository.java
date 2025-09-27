/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl; 

import com.mycompany.summoneranalyzer.entity.impl.Favorite;
import com.mycompany.summoneranalyzer.repository.MyAppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional; 
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRepository implements MyAppRepository<Favorite, Long> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Favorite> findAll() {
        return em.createQuery("SELECT f FROM Favorite f", Favorite.class).getResultList();
    }

    public List<Favorite> findAllByUserId(Long userId) {
        return em.createQuery(
                "SELECT f FROM Favorite f WHERE f.user.id = :uid ORDER BY f.createdAt DESC", Favorite.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public Optional<Favorite> findByUserIdAndSummonerId(Long userId, Long summonerId) {
        List<Favorite> list = em.createQuery(
            "SELECT f FROM Favorite f WHERE f.user.id = :u AND f.summoner.id = :s", Favorite.class)
            .setParameter("u", userId)
            .setParameter("s", summonerId)
            .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Favorite findById(Long id) throws Exception {
        Favorite f = em.find(Favorite.class, id);
        if (f == null) throw new Exception("Favorite not found: " + id);
        return f;
    }

    @Override @Transactional
    public void save(Favorite entity) {
        if (entity.getId() == null) em.persist(entity);
        else em.merge(entity);
    }

    @Override @Transactional
    public void deleteById(Long id) {
        Favorite f = em.find(Favorite.class, id);
        if (f != null) em.remove(f);
    }
}
