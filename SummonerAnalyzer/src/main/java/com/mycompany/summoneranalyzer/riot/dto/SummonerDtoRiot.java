/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.riot.dto;  

public class SummonerDtoRiot {
    private String id;       // encryptedSummonerId
    private String accountId;
    private String puuid;
    private String name;
    private Integer profileIconId;
    private Long revisionDate;
    private Integer summonerLevel;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getPuuid() { return puuid; }
    public void setPuuid(String puuid) { this.puuid = puuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getProfileIconId() { return profileIconId; }
    public void setProfileIconId(Integer profileIconId) { this.profileIconId = profileIconId; }
    public Long getRevisionDate() { return revisionDate; }
    public void setRevisionDate(Long revisionDate) { this.revisionDate = revisionDate; }
    public Integer getSummonerLevel() { return summonerLevel; }
    public void setSummonerLevel(Integer summonerLevel) { this.summonerLevel = summonerLevel; }
}

