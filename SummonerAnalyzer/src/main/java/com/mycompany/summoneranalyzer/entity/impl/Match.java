 
package com.mycompany.summoneranalyzer.entity.impl;

 

import com.mycompany.summoneranalyzer.entity.impl.enums.GameType;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import jakarta.persistence.*;
import java.time.LocalDateTime; 

@Entity
@Table(name = "match")
public class Match {

    @Id
    @Column(length = 30)
    private String id; // npr. EUN1_123456789

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=8)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private GameType gameType;

    @Column(nullable=false)
    private int durationSec;

    @Column(nullable=false)
    private LocalDateTime startedAt;

    public Match() {}
    public Match(String id) { this.id = id; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
    public GameType getGameType() { return gameType; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }
    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
