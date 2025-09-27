 
package com.mycompany.summoneranalyzer.dto.impl;
 
import com.mycompany.summoneranalyzer.dto.Dto;

 

public class MatchSummaryDto implements Dto {
    private Long id;
    private String matchId;     
    private Long summonerId;     
    private String champion;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Boolean win;

    public MatchSummaryDto() {}

    public MatchSummaryDto(Long id, String matchId, Long summonerId, String champion,
                           Integer kills, Integer deaths, Integer assists, Boolean win) {
        this.id = id; this.matchId = matchId; this.summonerId = summonerId;
        this.champion = champion; this.kills = kills; this.deaths = deaths;
        this.assists = assists; this.win = win;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public Long getSummonerId() { return summonerId; }
    public void setSummonerId(Long summonerId) { this.summonerId = summonerId; }
    public String getChampion() { return champion; }
    public void setChampion(String champion) { this.champion = champion; }
    public Integer getKills() { return kills; }
    public void setKills(Integer kills) { this.kills = kills; }
    public Integer getDeaths() { return deaths; }
    public void setDeaths(Integer deaths) { this.deaths = deaths; }
    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }
    public Boolean getWin() { return win; }
    public void setWin(Boolean win) { this.win = win; }
}
