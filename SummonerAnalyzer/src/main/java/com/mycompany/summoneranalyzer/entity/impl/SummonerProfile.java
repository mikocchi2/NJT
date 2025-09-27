package com.mycompany.summoneranalyzer.entity.impl;

import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import jakarta.persistence.*;
import java.time.LocalDateTime;
 

@Entity
@Table(name = "summoner_profile",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_sp_puuid", columnNames = "puuid")
       },
       indexes = {
           @Index(name="ix_sp_name_region", columnList = "name,region")
       })
public class SummonerProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=80, unique=true)
    private String puuid;

    @Column(nullable=false, length=40)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=8)
    private Region region;

    @Column(nullable=false)
    private int level;

    @Column(nullable=true, length=20)
    private String rankTier;      // npr. GOLD/PLATINUM

    @Column(nullable=true, length=5)
    private String rankDivision;  // npr. II

    @Column(nullable=true)
    private Integer leaguePoints;

    @Column(nullable=false)
    private LocalDateTime lastSyncedAt = LocalDateTime.now();

    public SummonerProfile() {}
    public SummonerProfile(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPuuid() { return puuid; }
    public void setPuuid(String puuid) { this.puuid = puuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getRankTier() { return rankTier; }
    public void setRankTier(String rankTier) { this.rankTier = rankTier; }

    public String getRankDivision() { return rankDivision; }
    public void setRankDivision(String rankDivision) { this.rankDivision = rankDivision; }

    public Integer getLeaguePoints() { return leaguePoints; }
    public void setLeaguePoints(Integer leaguePoints) { this.leaguePoints = leaguePoints; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
