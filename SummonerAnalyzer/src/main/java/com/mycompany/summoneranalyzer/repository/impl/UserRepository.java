/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository.impl; 

import com.mycompany.summoneranalyzer.entity.impl.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    public User findByEmail(String email) {
        List<User> list = em.createQuery("SELECT u FROM User u WHERE lower(u.email)=lower(:e)", User.class)
                .setParameter("e", email)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public User findById(Long id) { return em.find(User.class, id); }

    public void save(User u) {
        if (u.getId() == null) em.persist(u);
        else em.merge(u);
    }
}
