/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.entity.impl;

 

import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import jakarta.persistence.*;
import java.time.LocalDateTime; 

@Entity
@Table(name = "api_call_log",
       indexes = {
           @Index(name="ix_api_region", columnList = "region"),
           @Index(name="ix_api_status", columnList = "status")
       })
public class ApiCallLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=80)
    private String endpoint;

    @Column(nullable=false)
    private int status;

    @Enumerated(EnumType.STRING)
    @Column(nullable=true, length=8)
    private Region region;

    @Column(nullable=true)
    private Long actorUserId; // ko je pokrenuo (nije obavezna veza)

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ApiCallLog() {}
    public ApiCallLog(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
