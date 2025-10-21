package com.mycompany.summoneranalyzer.dto.impl;

import com.mycompany.summoneranalyzer.dto.Dto;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SummonerProfileDto implements Dto {
    private Long id;
    private String puuid;
    private String name;
    private Region region;
    private Integer level;
    private String rankTier;
    private String rankDivision;
    private Integer leaguePoints;
    private LocalDateTime lastSyncedAt; // read-only iz entiteta
    private Integer iconId;
    private List<RecentMatchDto> matches;

    public SummonerProfileDto() {
        this.matches = new ArrayList<>();
    }

    public SummonerProfileDto(Long id, String puuid, String name, Region region, Integer level,
                              String rankTier, String rankDivision, Integer leaguePoints,
                              LocalDateTime lastSyncedAt) {
        this(id, puuid, name, region, level, rankTier, rankDivision, leaguePoints, lastSyncedAt, null, new ArrayList<>());
    }

    public SummonerProfileDto(Long id, String puuid, String name, Region region, Integer level,
                              String rankTier, String rankDivision, Integer leaguePoints,
                              LocalDateTime lastSyncedAt, Integer iconId, List<RecentMatchDto> matches) {
        this.id = id;
        this.puuid = puuid;
        this.name = name;
        this.region = region;
        this.level = level;
        this.rankTier = rankTier;
        this.rankDivision = rankDivision;
        this.leaguePoints = leaguePoints;
        this.lastSyncedAt = lastSyncedAt;
        this.iconId = iconId;
        this.matches = (matches != null) ? new ArrayList<>(matches) : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPuuid() { return puuid; }
    public void setPuuid(String puuid) { this.puuid = puuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getRankTier() { return rankTier; }
    public void setRankTier(String rankTier) { this.rankTier = rankTier; }
    public String getRankDivision() { return rankDivision; }
    public void setRankDivision(String rankDivision) { this.rankDivision = rankDivision; }
    public Integer getLeaguePoints() { return leaguePoints; }
    public void setLeaguePoints(Integer leaguePoints) { this.leaguePoints = leaguePoints; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
    public Integer getIconId() { return iconId; }
    public void setIconId(Integer iconId) { this.iconId = iconId; }
    public List<RecentMatchDto> getMatches() { return matches; }
    public void setMatches(List<RecentMatchDto> matches) {
        this.matches = (matches != null) ? new ArrayList<>(matches) : new ArrayList<>();
    }
}
