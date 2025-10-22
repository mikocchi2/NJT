package com.mycompany.summoneranalyzer.dto.impl;

import com.mycompany.summoneranalyzer.entity.impl.enums.Region;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SummonerMatchHistoryDto {

    private String puuid;
    private String name;
    private String riotId;
    private String summonerName;
    private Region region;
    private Integer level;
    private Integer iconId;
    private String rankTier;
    private String rankDivision;
    private Integer leaguePoints;
    private Instant fetchedAt;
    private List<MatchSummary> matches = new ArrayList<>();

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRiotId() {
        return riotId;
    }

    public void setRiotId(String riotId) {
        this.riotId = riotId;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getIconId() {
        return iconId;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public String getRankTier() {
        return rankTier;
    }

    public void setRankTier(String rankTier) {
        this.rankTier = rankTier;
    }

    public String getRankDivision() {
        return rankDivision;
    }

    public void setRankDivision(String rankDivision) {
        this.rankDivision = rankDivision;
    }

    public Integer getLeaguePoints() {
        return leaguePoints;
    }

    public void setLeaguePoints(Integer leaguePoints) {
        this.leaguePoints = leaguePoints;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public List<MatchSummary> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchSummary> matches) {
        this.matches = matches != null ? new ArrayList<>(matches) : new ArrayList<>();
    }

    public static class MatchSummary {
        private String id;
        private Integer queueId;
        private Integer gameDuration;
        private Instant gameCreation;
        private String championName;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Boolean win;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getQueueId() {
            return queueId;
        }

        public void setQueueId(Integer queueId) {
            this.queueId = queueId;
        }

        public Integer getGameDuration() {
            return gameDuration;
        }

        public void setGameDuration(Integer gameDuration) {
            this.gameDuration = gameDuration;
        }

        public Instant getGameCreation() {
            return gameCreation;
        }

        public void setGameCreation(Instant gameCreation) {
            this.gameCreation = gameCreation;
        }

        public String getChampionName() {
            return championName;
        }

        public void setChampionName(String championName) {
            this.championName = championName;
        }

        public Integer getKills() {
            return kills;
        }

        public void setKills(Integer kills) {
            this.kills = kills;
        }

        public Integer getDeaths() {
            return deaths;
        }

        public void setDeaths(Integer deaths) {
            this.deaths = deaths;
        }

        public Integer getAssists() {
            return assists;
        }

        public void setAssists(Integer assists) {
            this.assists = assists;
        }

        public Boolean getWin() {
            return win;
        }

        public void setWin(Boolean win) {
            this.win = win;
        }
    }
}
