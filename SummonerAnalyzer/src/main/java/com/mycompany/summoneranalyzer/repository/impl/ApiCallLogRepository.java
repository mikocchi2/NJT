/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl; 

import com.mycompany.summoneranalyzer.entity.impl.ApiCallLog;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.repository.MyAppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List; 
import org.springframework.stereotype.Repository;

@Repository
public class ApiCallLogRepository implements MyAppRepository<ApiCallLog, Long> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ApiCallLog> findAll() {
        return em.createQuery("SELECT a FROM ApiCallLog a ORDER BY a.createdAt DESC", ApiCallLog.class)
                 .getResultList();
    }

    @Override
    public ApiCallLog findById(Long id) throws Exception {
        ApiCallLog a = em.find(ApiCallLog.class, id);
        if (a == null) throw new Exception("ApiCallLog not found: " + id);
        return a;
    }

    public List<ApiCallLog> filter(Integer status, Region region, LocalDateTime from, LocalDateTime to) {
        String jpql = "SELECT a FROM ApiCallLog a WHERE 1=1 " +
                      (status != null ? "AND a.status = :st " : "") +
                      (region != null ? "AND a.region = :rg " : "") +
                      (from != null ? "AND a.createdAt >= :from " : "") +
                      (to != null ? "AND a.createdAt <= :to " : "") +
                      "ORDER BY a.createdAt DESC";
        var q = em.createQuery(jpql, ApiCallLog.class);
        if (status != null) q.setParameter("st", status);
        if (region != null) q.setParameter("rg", region);
        if (from != null) q.setParameter("from", from);
        if (to != null) q.setParameter("to", to);
        return q.getResultList();
    }

    @Override @Transactional
    public void save(ApiCallLog entity) {
        if (entity.getId() == null) em.persist(entity);
        else em.merge(entity);
    }

    @Override @Transactional
    public void deleteById(Long id) {
        ApiCallLog a = em.find(ApiCallLog.class, id);
        if (a != null) em.remove(a);
    }
}
