/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.riot.dto;

public class LeagueEntryDtoRiot {
    private String leagueId;
    private String queueType;   // npr. RANKED_SOLO_5x5
    private String tier;        // GOLD/PLAT/...
    private String rank;        // I/II/III/IV
    private Integer leaguePoints;

    public String getLeagueId() { return leagueId; }
    public void setLeagueId(String leagueId) { this.leagueId = leagueId; }
    public String getQueueType() { return queueType; }
    public void setQueueType(String queueType) { this.queueType = queueType; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public Integer getLeaguePoints() { return leaguePoints; }
    public void setLeaguePoints(Integer leaguePoints) { this.leaguePoints = leaguePoints; }
}
