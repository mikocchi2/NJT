/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.mapper.impl;

 
 
import com.mycompany.summoneranalyzer.dto.impl.ApiCallLogDto;
import com.mycompany.summoneranalyzer.entity.impl.ApiCallLog;
import com.mycompany.summoneranalyzer.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class ApiCallLogMapper implements DtoEntityMapper<ApiCallLogDto, ApiCallLog> {

    @Override
    public ApiCallLogDto toDto(ApiCallLog e) {
        if (e == null) return null;
        return new ApiCallLogDto(
            e.getId(), e.getEndpoint(), e.getStatus(), e.getRegion(),
            e.getActorUserId(), e.getCreatedAt()
        );
    }

    @Override
    public ApiCallLog toEntity(ApiCallLogDto t) {
        ApiCallLog e = new ApiCallLog();
        e.setId(t.getId());
        e.setEndpoint(t.getEndpoint());
        if (t.getStatus() != null) e.setStatus(t.getStatus());
        e.setRegion(t.getRegion());
        e.setActorUserId(t.getActorUserId());
        if (t.getCreatedAt() != null) e.setCreatedAt(t.getCreatedAt());
        return e;
    }
}
