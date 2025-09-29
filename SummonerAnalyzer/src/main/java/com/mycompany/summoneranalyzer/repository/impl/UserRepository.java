/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl; 

import com.mycompany.summoneranalyzer.entity.impl.User;
import com.mycompany.summoneranalyzer.repository.MyAppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository implements MyAppRepository<User, Long> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    @Override
    public User findById(Long id) { return em.find(User.class, id); }

    public User findByEmail(String email) {
        var list = em.createQuery("SELECT u FROM User u WHERE u.email = :e", User.class)
                     .setParameter("e", email)
                     .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override @Transactional
    public void save(User entity) {
        if (entity.getId() == null) em.persist(entity);
        else em.merge(entity);
    }

    @Override @Transactional
    public void deleteById(Long id) {
        User u = em.find(User.class, id);
        if (u != null) em.remove(u);
    }
}
