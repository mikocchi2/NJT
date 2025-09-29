package com.mycompany.summoneranalyzer.riot.dto;

import java.util.List;

public class MatchV5DtoRiot {

    private Info info;

    public Info getInfo() { return info; }
    public void setInfo(Info info) { this.info = info; }

    public static class Info {
        private long gameCreation;        // epoch ms
        private long gameStartTimestamp;  // epoch ms
        private int gameDuration;         // seconds
        private int queueId;
        private String gameMode;          // ARAM / CLASSIC / ...

        private List<Participant> participants;

        public long getGameCreation() { return gameCreation; }
        public void setGameCreation(long gameCreation) { this.gameCreation = gameCreation; }
        public long getGameStartTimestamp() { return gameStartTimestamp; }
        public void setGameStartTimestamp(long gameStartTimestamp) { this.gameStartTimestamp = gameStartTimestamp; }
        public int getGameDuration() { return gameDuration; }
        public void setGameDuration(int gameDuration) { this.gameDuration = gameDuration; }
        public int getQueueId() { return queueId; }
        public void setQueueId(int queueId) { this.queueId = queueId; }
        public String getGameMode() { return gameMode; }
        public void setGameMode(String gameMode) { this.gameMode = gameMode; }
        public List<Participant> getParticipants() { return participants; }
        public void setParticipants(List<Participant> participants) { this.participants = participants; }
    }

    public static class Participant {
        private String puuid;
        private String championName;
        private int kills;
        private int deaths;
        private int assists;
        private boolean win;

        public String getPuuid() { return puuid; }
        public void setPuuid(String puuid) { this.puuid = puuid; }
        public String getChampionName() { return championName; }
        public void setChampionName(String championName) { this.championName = championName; }
        public int getKills() { return kills; }
        public void setKills(int kills) { this.kills = kills; }
        public int getDeaths() { return deaths; }
        public void setDeaths(int deaths) { this.deaths = deaths; }
        public int getAssists() { return assists; }
        public void setAssists(int assists) { this.assists = assists; }
        public boolean isWin() { return win; }
        public void setWin(boolean win) { this.win = win; }
    }
}
