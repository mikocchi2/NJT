/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.riot.dto; 
import java.util.Map;
public class MatchV5DtoRiot {
    private Info info;
    public Info getInfo(){return info;} public void setInfo(Info info){this.info=info;}
    public static class Info {
        private String gameMode;   // "ARAM", "CLASSIC"...
        private int gameDuration;  // sekunde
        private long gameStartTimestamp;
        private java.util.List<Participant> participants;
        public String getGameMode(){return gameMode;} public void setGameMode(String v){this.gameMode=v;}
        public int getGameDuration(){return gameDuration;} public void setGameDuration(int v){this.gameDuration=v;}
        public long getGameStartTimestamp(){return gameStartTimestamp;} public void setGameStartTimestamp(long v){this.gameStartTimestamp=v;}
        public java.util.List<Participant> getParticipants(){return participants;} public void setParticipants(java.util.List<Participant> p){this.participants=p;}
    }
    public static class Participant {
        private String puuid;
        private String championName;
        private int kills; private int deaths; private int assists;
        private boolean win;
        public String getPuuid(){return puuid;} public void setPuuid(String v){this.puuid=v;}
        public String getChampionName(){return championName;} public void setChampionName(String v){this.championName=v;}
        public int getKills(){return kills;} public void setKills(int v){this.kills=v;}
        public int getDeaths(){return deaths;} public void setDeaths(int v){this.deaths=v;}
        public int getAssists(){return assists;} public void setAssists(int v){this.assists=v;}
        public boolean isWin(){return win;} public void setWin(boolean v){this.win=v;}
    }
}

