package com.mycompany.summoneranalyzer.servis;

import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.dto.impl.RecentMatchDto;
import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.enums.GameType;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.riot.RiotApiClient;
import com.mycompany.summoneranalyzer.riot.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class SummonerSyncService {

    private static final Logger log = LoggerFactory.getLogger(SummonerSyncService.class);

    private final RiotApiClient riot;
    private final SummonerProfileService summoners;
    private final MatchService matches;
    private final MatchSummaryService summaries;
    private final ApiLogService apiLog;

    public SummonerSyncService(
            RiotApiClient riot,
            SummonerProfileService summoners,
            MatchService matches,
            MatchSummaryService summaries,
            ApiLogService apiLog
    ) {
        this.riot = riot;
        this.summoners = summoners;
        this.matches = matches;
        this.summaries = summaries;
        this.apiLog = apiLog;
    }

    /** POST /api/summoners/sync?name=<RiotID_ili_SummonerName>&region=EUNE&lastN=10 */
    @Transactional
    public SummonerProfileDto syncByName(String name, Region region, int lastN) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Ime summoner-a je obavezno");
        if (name.contains("#")) {
            return syncByRiotId(name, region, lastN);
        }
        SummonerDtoRiot summonerRiot = riot.getSummonerByName(name, region)
            .doOnSuccess(r -> apiLog.ok("/summoner/by-name", region))
            .doOnError(e -> apiLog.fail("/summoner/by-name", region))
            .block();
        if (summonerRiot == null) throw new Exception("Summoner po imenu nije pronađen: " + name);
        AccountDtoRiot account = riot.getAccountByPuuid(summonerRiot.getPuuid(), region)
            .doOnSuccess(r -> apiLog.ok("/riot/account/by-puuid", region))
            .doOnError(e -> apiLog.fail("/riot/account/by-puuid", region))
            .onErrorResume(ex -> Mono.empty())
            .block();
        return syncFromSummoner(summonerRiot, region, lastN, account);
    }

    /** "Caps#000" → (gameName, tagLine) */
    @Transactional
    public SummonerProfileDto syncByRiotId(String riotId, Region region, int lastN) throws Exception {
        if (!riotId.contains("#")) throw new Exception("Riot ID format: gameName#tagLine");
        String[] parts = riotId.split("#", 2);
        return syncByRiotId(parts[0], parts[1], region, lastN);
    }

    @Transactional
    public SummonerProfileDto syncByRiotId(String gameName, String tagLine, Region region, int lastN) throws Exception {
        AccountDtoRiot acc = riot.getAccountByRiotId(gameName, tagLine, region)
            .doOnSuccess(r -> apiLog.ok("/riot/account/by-riot-id", region))
            .doOnError(e -> apiLog.fail("/riot/account/by-riot-id", region))
            .block();
        if (acc == null || acc.getPuuid() == null) {
            throw new Exception("Nije pronađen Riot nalog za: " + gameName + "#" + tagLine);
        }

        SummonerDtoRiot summonerRiot = riot.getSummonerByPuuid(acc.getPuuid(), region)
            .doOnSuccess(r -> apiLog.ok("/summoner/by-puuid", region))
            .doOnError(e -> apiLog.fail("/summoner/by-puuid", region))
            .block();
        if (summonerRiot == null) throw new Exception("Summoner za PUUID nije pronađen");

        return syncFromSummoner(summonerRiot, region, lastN, acc);
    }

    @Transactional
    public SummonerProfileDto syncByPuuid(String puuid, Region region, int lastN) throws Exception {
        if (puuid == null || puuid.isBlank()) throw new Exception("PUUID je obavezan");

        SummonerDtoRiot summonerRiot = riot.getSummonerByPuuid(puuid, region)
            .doOnSuccess(r -> apiLog.ok("/summoner/by-puuid", region))
            .doOnError(e -> apiLog.fail("/summoner/by-puuid", region))
            .block();
        if (summonerRiot == null) throw new Exception("Summoner za PUUID nije pronađen");

        AccountDtoRiot account = riot.getAccountByPuuid(puuid, region)
            .doOnSuccess(r -> apiLog.ok("/riot/account/by-puuid", region))
            .doOnError(e -> apiLog.fail("/riot/account/by-puuid", region))
            .onErrorResume(ex -> Mono.empty())
            .block();

        return syncFromSummoner(summonerRiot, region, lastN, account);
    }

    /* ================= Core ================= */

    private SummonerProfileDto syncFromSummoner(SummonerDtoRiot s, Region region, int lastN) throws Exception {
        return syncFromSummoner(s, region, lastN, null);
    }

    private SummonerProfileDto syncFromSummoner(SummonerDtoRiot s, Region region, int lastN, AccountDtoRiot account) throws Exception {
        // LEAGUE by-PUUID (po tvojim dozvolama)
        List<LeagueEntryDtoRiot> leagues = riot.getLeagueByPuuid(s.getPuuid(), region)
            .doOnSuccess(r -> apiLog.ok("/league/by-puuid", region))
            .doOnError(e -> apiLog.fail("/league/by-puuid", region))
            .block();

        String displayName = resolveDisplayName(account, s);
        SummonerProfileDto sp = new SummonerProfileDto(
            null,
            s.getPuuid(),
            displayName,
            region,
            s.getSummonerLevel(),
            extractTier(leagues),
            extractDivision(leagues),
            extractLP(leagues),
            LocalDateTime.now()
        );
        SummonerProfileDto saved = summoners.upsert(sp);

        List<RecentMatchDto> recentMatches = new ArrayList<>();

        List<String> matchIds = riot.getRecentMatchIds(s.getPuuid(), region, lastN)
            .doOnSuccess(r -> apiLog.ok("/match/ids", region))
            .doOnError(e -> apiLog.fail("/match/ids", region))
            .block();

        if (matchIds == null || matchIds.isEmpty()) {
            log.info("[SummonerSync] No recent matches returned for PUUID={} (region={}, lastN={})", s.getPuuid(), region, lastN);
        } else {
            log.info("[SummonerSync] Recent match IDs for PUUID={} (region={}, lastN={}): {}", s.getPuuid(), region, lastN, matchIds);

            for (String mid : matchIds) {
                MatchV5DtoRiot match = riot.getMatch(mid, region)
                    .doOnSuccess(r -> apiLog.ok("/match/" + mid, region))
                    .doOnError(e -> apiLog.fail("/match/" + mid, region))
                    .block();
                if (match == null) {
                    log.warn("[SummonerSync] Riot API returned empty match payload for matchId={} (puuid={}, region={})", mid, s.getPuuid(), region);
                    continue;
                }

                log.info("[SummonerSync] Fetched match {} for puuid={} (queueId={}, gameDuration={})",
                        mid,
                        s.getPuuid(),
                        match.getInfo() != null ? match.getInfo().getQueueId() : null,
                        match.getInfo() != null ? match.getInfo().getGameDuration() : null
                );

                MatchDto m = mapMatch(mid, match, region);
                matches.upsert(m);
                log.info("[SummonerSync] Saved match {} to database (region={}, gameType={})", mid, region, m.getGameType());

                MatchSummaryDto ms = mapSummaryForPuuid(mid, match, saved.getId(), saved.getPuuid());
                if (ms != null) {
                    summaries.upsertForMatchAndSummoner(ms);
                    log.info("[SummonerSync] Upserted match summary for matchId={} and summonerId={} (win={}, kda={}/{}/{})",
                            mid, saved.getId(), ms.getWin(), ms.getKills(), ms.getDeaths(), ms.getAssists());
                }

                RecentMatchDto recent = mapRecentMatch(mid, match, saved.getPuuid());
                if (recent != null) {
                    recentMatches.add(recent);
                }
            }
        }
        saved.setMatches(recentMatches);
        saved.setIconId(s.getProfileIconId());
        return saved;
    }

    /* ================= Helpers ================= */

    private String resolveDisplayName(AccountDtoRiot account, SummonerDtoRiot summoner) {
        if (account != null) {
            String gameName = account.getGameName();
            String tagLine = account.getTagLine();
            if (gameName != null && !gameName.isBlank() && tagLine != null && !tagLine.isBlank()) {
                return gameName + "#" + tagLine;
            }
            if (gameName != null && !gameName.isBlank()) {
                return gameName;
            }
        }
        return summoner.getName();
    }

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

        // preciznije bi bilo po queueId; zadržavam tvoju grubu mapu
        GameType gt = switch (info.getGameMode()) {
            case "ARAM"    -> GameType.ARAM;
            case "CLASSIC" -> GameType.RANKED; // može biti i NORMAL; koristi queueId po želji
            default        -> GameType.NORMAL;
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

    private RecentMatchDto mapRecentMatch(String matchId, MatchV5DtoRiot match, String puuid) {
        if (match == null || match.getInfo() == null) {
            return null;
        }

        var info = match.getInfo();
        var participant = info.getParticipants().stream()
            .filter(x -> puuid.equals(x.getPuuid()))
            .findFirst()
            .orElse(null);
        if (participant == null) {
            return null;
        }

        RecentMatchDto dto = new RecentMatchDto();
        dto.setId(matchId);
        dto.setQueueId(info.getQueueId());
        dto.setGameDuration(info.getGameDuration());
        dto.setGameCreation(Instant.ofEpochMilli(info.getGameCreation()));
        dto.setChampionName(participant.getChampionName());
        dto.setKills(participant.getKills());
        dto.setDeaths(participant.getDeaths());
        dto.setAssists(participant.getAssists());
        dto.setWin(participant.isWin());
        return dto;
    }
}
