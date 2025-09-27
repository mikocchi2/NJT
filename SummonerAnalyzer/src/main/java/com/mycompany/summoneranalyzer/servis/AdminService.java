/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis;
 
import com.mycompany.summoneranalyzer.dto.impl.ApiCallLogDto;
import com.mycompany.summoneranalyzer.entity.impl.ApiCallLog;
import com.mycompany.summoneranalyzer.entity.impl.User;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.mapper.impl.ApiCallLogMapper;
import com.mycompany.summoneranalyzer.repository.impl.ApiCallLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; 
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class AdminService {

    private final ApiCallLogRepository logs;
    private final ApiCallLogMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public AdminService(ApiCallLogRepository logs, ApiCallLogMapper mapper) {
        this.logs = logs; this.mapper = mapper;
    }

    public List<ApiCallLogDto> listLogs(Integer status, Region region, LocalDateTime from, LocalDateTime to) {
        return logs.filter(status, region, from, to).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public List<User> listUsers() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.createdAt DESC", User.class).getResultList();
    }

    @Transactional
    public ApiCallLogDto append(ApiCallLogDto dto) {
        ApiCallLog e = mapper.toEntity(dto);
        logs.save(e);
        return mapper.toDto(e);
    }

    @Transactional
    public void deleteLog(Long id) { logs.deleteById(id); }

    @Transactional
    public void deleteUser(Long userId) {
        User u = em.find(User.class, userId);
        if (u != null) em.remove(u);
    }
}
