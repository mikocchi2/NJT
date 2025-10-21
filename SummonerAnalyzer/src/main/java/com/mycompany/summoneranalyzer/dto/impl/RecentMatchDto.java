package com.mycompany.summoneranalyzer.dto.impl;

import com.mycompany.summoneranalyzer.dto.Dto;

import java.time.Instant;

public class RecentMatchDto implements Dto {
    private String id;
    private Integer queueId;
    private Integer gameDuration;
    private Instant gameCreation;
    private String championName;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Boolean win;

    public RecentMatchDto() {}

    public RecentMatchDto(String id, Integer queueId, Integer gameDuration, Instant gameCreation,
                          String championName, Integer kills, Integer deaths, Integer assists, Boolean win) {
        this.id = id;
        this.queueId = queueId;
        this.gameDuration = gameDuration;
        this.gameCreation = gameCreation;
        this.championName = championName;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.win = win;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getQueueId() { return queueId; }
    public void setQueueId(Integer queueId) { this.queueId = queueId; }
    public Integer getGameDuration() { return gameDuration; }
    public void setGameDuration(Integer gameDuration) { this.gameDuration = gameDuration; }
    public Instant getGameCreation() { return gameCreation; }
    public void setGameCreation(Instant gameCreation) { this.gameCreation = gameCreation; }
    public String getChampionName() { return championName; }
    public void setChampionName(String championName) { this.championName = championName; }
    public Integer getKills() { return kills; }
    public void setKills(Integer kills) { this.kills = kills; }
    public Integer getDeaths() { return deaths; }
    public void setDeaths(Integer deaths) { this.deaths = deaths; }
    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }
    public Boolean getWin() { return win; }
    public void setWin(Boolean win) { this.win = win; }
}
