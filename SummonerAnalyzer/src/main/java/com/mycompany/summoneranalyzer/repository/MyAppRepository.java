/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.summoneranalyzer.repository;

import java.util.List;

/**
 *
 * @author vanja
 */
public interface MyAppRepository<E, ID> {
 
    List<E> findAll();
    E findById(ID id) throws Exception;
    void save(E entity);
    void deleteById(ID id);
    
}
