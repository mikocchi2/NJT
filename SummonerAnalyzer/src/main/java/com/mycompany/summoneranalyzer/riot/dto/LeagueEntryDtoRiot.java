/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.riot.dto;

public class LeagueEntryDtoRiot {
    private String queueType; // "RANKED_SOLO_5x5"
    private String tier;      // GOLD/PLATINUM
    private String rank;      // I/II/III/IV
    private Integer leaguePoints;
    public String getQueueType(){return queueType;} public void setQueueType(String v){this.queueType=v;}
    public String getTier(){return tier;} public void setTier(String v){this.tier=v;}
    public String getRank(){return rank;} public void setRank(String v){this.rank=v;}
    public Integer getLeaguePoints(){return leaguePoints;} public void setLeaguePoints(Integer v){this.leaguePoints=v;}
}
