/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.riot.dto; 
public class SummonerDtoRiot {
    private String id;      // encryptedSummonerId (za league-v4)
    private String puuid;   // za match-v5
    private String name;
    private int summonerLevel;
    public String getId(){return id;} public void setId(String id){this.id=id;}
    public String getPuuid(){return puuid;} public void setPuuid(String puuid){this.puuid=puuid;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public int getSummonerLevel(){return summonerLevel;} public void setSummonerLevel(int v){this.summonerLevel=v;}
}
