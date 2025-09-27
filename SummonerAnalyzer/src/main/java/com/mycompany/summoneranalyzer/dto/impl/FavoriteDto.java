 
package com.mycompany.summoneranalyzer.dto.impl;
 

import com.mycompany.summoneranalyzer.dto.Dto;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
 

public class FavoriteDto implements Dto {
    private Long id;

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "summonerId is required")
    private Long summonerId;

    private String note;
    private LocalDateTime createdAt;

    public FavoriteDto() {}

    public FavoriteDto(Long id, Long userId, Long summonerId, String note, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.summonerId = summonerId;
        this.note = note; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSummonerId() { return summonerId; }
    public void setSummonerId(Long summonerId) { this.summonerId = summonerId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
