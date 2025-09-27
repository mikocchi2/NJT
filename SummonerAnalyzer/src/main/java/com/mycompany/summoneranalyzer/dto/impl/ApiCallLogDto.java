/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.dto.impl;
 

import com.mycompany.summoneranalyzer.dto.Dto;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import java.time.LocalDateTime;
 

public class ApiCallLogDto implements Dto {
    private Long id;
    private String endpoint;
    private Integer status;
    private Region region;
    private Long actorUserId;
    private LocalDateTime createdAt;

    public ApiCallLogDto() {}
    public ApiCallLogDto(Long id, String endpoint, Integer status, Region region,
                         Long actorUserId, LocalDateTime createdAt) {
        this.id = id; this.endpoint = endpoint; this.status = status;
        this.region = region; this.actorUserId = actorUserId; this.createdAt = createdAt;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEndpoint() { return endpoint; } public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Region getRegion() { return region; } public void setRegion(Region region) { this.region = region; }
    public Long getActorUserId() { return actorUserId; } public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
