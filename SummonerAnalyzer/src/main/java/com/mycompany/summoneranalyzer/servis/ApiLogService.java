/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis; 

import com.mycompany.summoneranalyzer.entity.impl.ApiCallLog;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.repository.impl.ApiCallLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiLogService {

    private final ApiCallLogRepository repo;

    public ApiLogService(ApiCallLogRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void ok(String endpoint, Region region) {
        save(endpoint, 200, region, null);
    }

    @Transactional
    public void fail(String endpoint, Region region) {
        // ako želiš, možeš proslediti tačan status; za sada 500
        save(endpoint, 500, region, null);
    }

    @Transactional
    public void save(String endpoint, int status, Region region, Long actorUserId) {
        ApiCallLog log = new ApiCallLog();
        log.setEndpoint(endpoint);
        log.setStatus(status);
        log.setRegion(region);
        log.setActorUserId(actorUserId);
        repo.save(log);
    }
}
