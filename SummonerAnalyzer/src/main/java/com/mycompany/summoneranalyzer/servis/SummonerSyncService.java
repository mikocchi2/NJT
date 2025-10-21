package com.mycompany.summoneranalyzer.servis;

import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.enums.GameType;
import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.riot.RiotApiClient;
import com.mycompany.summoneranalyzer.riot.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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

        List<String> matchIds = riot.getRecentMatchIds(s.getPuuid(), region, lastN)
            .doOnSuccess(r -> apiLog.ok("/match/ids", region))
            .doOnError(e -> apiLog.fail("/match/ids", region))
            .block();

        if (matchIds != null) {
            for (String mid : matchIds) {
                MatchV5DtoRiot match = riot.getMatch(mid, region)
                    .doOnSuccess(r -> apiLog.ok("/match/" + mid, region))
                    .doOnError(e -> apiLog.fail("/match/" + mid, region))
                    .block();
                if (match == null) continue;

                MatchDto m = mapMatch(mid, match, region);
                matches.upsert(m);

                MatchSummaryDto ms = mapSummaryForPuuid(mid, match, saved.getId(), saved.getPuuid());
                if (ms != null) summaries.upsertForMatchAndSummoner(ms);
            }
        }
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
}
