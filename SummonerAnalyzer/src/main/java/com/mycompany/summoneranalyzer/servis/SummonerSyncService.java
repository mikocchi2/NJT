/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.servis;  

import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.enums.GameType;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.riot.RiotApiClient;
import com.mycompany.summoneranalyzer.riot.dto.LeagueEntryDtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.MatchV5DtoRiot;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SummonerSyncService {

    private final RiotApiClient riot;
    private final SummonerProfileService summoners;
    private final MatchService matches;
    private final MatchSummaryService summaries;
    private final ApiLogService apiLog;

    public SummonerSyncService(RiotApiClient riot,
                               SummonerProfileService summoners,
                               MatchService matches,
                               MatchSummaryService summaries,
                               ApiLogService apiLog) {
        this.riot = riot; this.summoners = summoners; this.matches = matches; this.summaries = summaries; this.apiLog = apiLog;
    }

    @Transactional
    public SummonerProfileDto syncByName(String name, Region region, int lastN) throws Exception {
        var summonerRiot = riot.getSummonerByName(name, region)
                .doOnSuccess(r -> apiLog.ok("/summoner/by-name", region))
                .doOnError(e -> apiLog.fail("/summoner/by-name", region))
                .block();

        if (summonerRiot == null) throw new Exception("Summoner not found on Riot");

        var leagues = riot.getLeagueBySummonerId(summonerRiot.getId(), region)
                .doOnSuccess(r -> apiLog.ok("/league/by-summoner", region))
                .doOnError(e -> apiLog.fail("/league/by-summoner", region))
                .block();

        var sp = new SummonerProfileDto(
                null,
                summonerRiot.getPuuid(),
                summonerRiot.getName(),
                region,
                summonerRiot.getSummonerLevel(),
                extractTier(leagues),
                extractDivision(leagues),
                extractLP(leagues),
                LocalDateTime.now()
        );
        var savedProfile = summoners.upsert(sp);

        var matchIds = riot.getRecentMatchIds(summonerRiot.getPuuid(), region, lastN)
                .doOnSuccess(r -> apiLog.ok("/match/ids", region))
                .doOnError(e -> apiLog.fail("/match/ids", region))
                .block();

        if (matchIds != null) {
            for (String mid : matchIds) {
                var matchDtoRiot = riot.getMatch(mid, region)
                        .doOnSuccess(r -> apiLog.ok("/match/" + mid, region))
                        .doOnError(e -> apiLog.fail("/match/" + mid, region))
                        .block();
                if (matchDtoRiot == null) continue;

                MatchDto m = mapMatch(mid, matchDtoRiot, region);
                matches.upsert(m);

                MatchSummaryDto ms = mapSummaryForPuuid(mid, matchDtoRiot, savedProfile.getId(), savedProfile.getPuuid());
                if (ms != null) summaries.create(ms);
            }
        }
        return savedProfile;
    }

    /* ===== Helpers ===== */

    private String extractTier(List<LeagueEntryDtoRiot> leagues) {
        return (leagues == null || leagues.isEmpty()) ? null : leagues.get(0).getTier();
    }
    private String extractDivision(List<LeagueEntryDtoRiot> leagues) {
        return (leagues == null || leagues.isEmpty()) ? null : leagues.get(0).getRank();
    }
    private Integer extractLP(List<LeagueEntryDtoRiot> leagues) {
        return (leagues == null || leagues.isEmpty()) ? null : leagues.get(0).getLeaguePoints();
    }

    private MatchDto mapMatch(String matchId, MatchV5DtoRiot m, Region region) {
        var info = m.getInfo();

        MatchDto dto = new MatchDto();
        dto.setId(matchId);
        dto.setRegion(region);
        dto.setDurationSec(info.getGameDuration());

        // Preciznije je koristiti queueId (ako ga dodaš u Riot DTO). Za sada grubo:
        GameType gt = switch (info.getGameMode()) {
            case "ARAM" -> GameType.ARAM;
            case "CLASSIC" -> GameType.RANKED; // može biti i NORMAL; zavisi od queueId
            default -> GameType.NORMAL;
        };
        dto.setGameType(gt);

        LocalDateTime started = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(info.getGameStartTimestamp()), ZoneOffset.UTC);
        dto.setStartedAt(started);

        return dto;
    }

    private MatchSummaryDto mapSummaryForPuuid(String matchId, MatchV5DtoRiot m, Long summonerDbId, String puuid) {
        var p = m.getInfo().getParticipants().stream()
                .filter(x -> puuid.equals(x.getPuuid()))
                .findFirst().orElse(null);
        if (p == null) return null;

        MatchSummaryDto ms = new MatchSummaryDto();
        ms.setMatchId(matchId);
        ms.setSummonerId(summonerDbId);
        ms.setChampion(p.getChampionName());
        ms.setKills(p.getKills());
        ms.setDeaths(p.getDeaths());
        ms.setAssists(p.getAssists());
        ms.setWin(p.isWin());
        return ms;
    }
}
