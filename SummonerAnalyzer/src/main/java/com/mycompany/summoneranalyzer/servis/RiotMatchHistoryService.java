package com.mycompany.summoneranalyzer.servis;

import com.mycompany.summoneranalyzer.dto.impl.SummonerMatchHistoryDto;
import com.mycompany.summoneranalyzer.dto.impl.SummonerMatchHistoryDto.MatchSummary;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.riot.RiotApiClient;
import com.mycompany.summoneranalyzer.riot.dto.AccountDtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.LeagueEntryDtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.MatchV5DtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.SummonerDtoRiot;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RiotMatchHistoryService {

    private final RiotApiClient riotApiClient;

    public RiotMatchHistoryService(RiotApiClient riotApiClient) {
        this.riotApiClient = riotApiClient;
    }

    public SummonerMatchHistoryDto fetchByRiotId(String riotId, Region region, int count) throws Exception {
        if (riotId == null || riotId.isBlank() || !riotId.contains("#")) {
            throw new Exception("Riot ID mora biti u formatu gameName#tagLine");
        }
        String[] parts = riotId.split("#", 2);
        return fetchByRiotId(parts[0], parts[1], region, count);
    }

    public SummonerMatchHistoryDto fetchByRiotId(String gameName, String tagLine, Region region, int count) throws Exception {
        if (gameName == null || gameName.isBlank() || tagLine == null || tagLine.isBlank()) {
            throw new Exception("gameName i tagLine su obavezni");
        }

        AccountDtoRiot account = riotApiClient.getAccountByRiotId(gameName, tagLine, region)
            .onErrorResume(ex -> Mono.error(new Exception("Riot nalog nije pronađen: " + ex.getMessage(), ex)))
            .block();

        if (account == null || account.getPuuid() == null || account.getPuuid().isBlank()) {
            throw new Exception("Riot nalog nije pronađen za zadati Riot ID");
        }

        SummonerDtoRiot summoner = riotApiClient.getSummonerByPuuid(account.getPuuid(), region)
            .block();
        if (summoner == null) {
            throw new Exception("Summoner nije pronađen za PUUID " + account.getPuuid());
        }

        List<LeagueEntryDtoRiot> leagues = Optional.ofNullable(
            riotApiClient.getLeagueByPuuid(account.getPuuid(), region).block()
        ).orElseGet(ArrayList::new);

        List<String> matchIds = Optional.ofNullable(
            riotApiClient.getRecentMatchIds(account.getPuuid(), region, Math.max(1, count)).block()
        ).orElseGet(ArrayList::new);

        List<MatchSummary> matches = new ArrayList<>();
        for (String matchId : matchIds) {
            MatchV5DtoRiot match = riotApiClient.getMatch(matchId, region).block();
            if (match == null || match.getInfo() == null || match.getInfo().getParticipants() == null) {
                continue;
            }
            MatchV5DtoRiot.Participant participant = match.getInfo().getParticipants().stream()
                .filter(p -> account.getPuuid().equals(p.getPuuid()))
                .findFirst()
                .orElse(null);
            if (participant == null) {
                continue;
            }
            MatchSummary summary = new MatchSummary();
            summary.setId(matchId);
            summary.setQueueId(match.getInfo().getQueueId());
            summary.setGameDuration(match.getInfo().getGameDuration());
            if (match.getInfo().getGameCreation() > 0) {
                summary.setGameCreation(Instant.ofEpochMilli(match.getInfo().getGameCreation()));
            }
            summary.setChampionName(participant.getChampionName());
            summary.setKills(participant.getKills());
            summary.setDeaths(participant.getDeaths());
            summary.setAssists(participant.getAssists());
            summary.setWin(participant.isWin());
            matches.add(summary);
        }

        matches.sort((a, b) -> {
            Instant aTime = a.getGameCreation();
            Instant bTime = b.getGameCreation();
            if (aTime == null && bTime == null) {
                return 0;
            }
            if (aTime == null) {
                return 1;
            }
            if (bTime == null) {
                return -1;
            }
            return bTime.compareTo(aTime);
        });

        SummonerMatchHistoryDto dto = new SummonerMatchHistoryDto();
        dto.setPuuid(account.getPuuid());
        dto.setName(resolveDisplayName(account, summoner));
        dto.setRiotId(resolveRiotId(account, gameName, tagLine));
        dto.setSummonerName(summoner.getName());
        dto.setRegion(region);
        dto.setLevel(summoner.getSummonerLevel());
        dto.setIconId(summoner.getProfileIconId());

        LeagueEntryDtoRiot preferred = selectPreferredLeague(leagues);
        if (preferred != null) {
            dto.setRankTier(preferred.getTier());
            dto.setRankDivision(preferred.getRank());
            dto.setLeaguePoints(preferred.getLeaguePoints());
        }

        dto.setFetchedAt(Instant.now());
        dto.setMatches(matches);
        return dto;
    }

    private String resolveDisplayName(AccountDtoRiot account, SummonerDtoRiot summoner) {
        if (account != null) {
            String gameName = account.getGameName();
            String tagLine = account.getTagLine();
            if (isNotBlank(gameName) && isNotBlank(tagLine)) {
                return gameName + "#" + tagLine;
            }
            if (isNotBlank(gameName)) {
                return gameName;
            }
        }
        return summoner != null ? summoner.getName() : "Unknown";
    }

    private String resolveRiotId(AccountDtoRiot account, String gameName, String tagLine) {
        if (account != null && isNotBlank(account.getGameName()) && isNotBlank(account.getTagLine())) {
            return account.getGameName() + "#" + account.getTagLine();
        }
        if (isNotBlank(gameName) && isNotBlank(tagLine)) {
            return gameName + "#" + tagLine;
        }
        return null;
    }

    private LeagueEntryDtoRiot selectPreferredLeague(List<LeagueEntryDtoRiot> leagues) {
        if (leagues == null || leagues.isEmpty()) {
            return null;
        }
        return leagues.stream()
            .filter(Objects::nonNull)
            .filter(l -> "RANKED_SOLO_5x5".equalsIgnoreCase(l.getQueueType()))
            .findFirst()
            .orElseGet(() -> leagues.stream().filter(Objects::nonNull).findFirst().orElse(null));
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
