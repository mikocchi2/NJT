 
package com.mycompany.summoneranalyzer.dto.impl;
 

import com.mycompany.summoneranalyzer.dto.Dto;
import com.mycompany.summoneranalyzer.entity.impl.enums.GameType;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import java.time.LocalDateTime; 

public class MatchDto implements Dto {
    private String id;
    private Region region;
    private GameType gameType;
    private Integer durationSec;
    private LocalDateTime startedAt;

    public MatchDto() {}

    public MatchDto(String id, Region region, GameType gameType, Integer durationSec, LocalDateTime startedAt) {
        this.id = id; this.region = region; this.gameType = gameType;
        this.durationSec = durationSec; this.startedAt = startedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
    public GameType getGameType() { return gameType; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
