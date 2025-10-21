package com.mycompany.summoneranalyzer.riot;

import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.riot.dto.AccountDtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.LeagueEntryDtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.MatchV5DtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.SummonerDtoRiot;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RiotApiClient {

    private final WebClient web;

    public RiotApiClient(WebClient riotWebClient) {
        this.web = riotWebClient;
    }

    /* ================== HOST MAPIRANJE ================== */

    /** PLATFORM (summoner-v4, league-v4) */
    private String platformHost(Region region) {
        return switch (region) {
            case EUNE -> "https://eun1.api.riotgames.com";
            case EUW  -> "https://euw1.api.riotgames.com";
            case NA1  -> "https://na1.api.riotgames.com";
            case KR   -> "https://kr.api.riotgames.com";
            case BR1  -> "https://br1.api.riotgames.com";
            case JP1  -> "https://jp1.api.riotgames.com";
            case OC1  -> "https://oc1.api.riotgames.com";
            case TR1  -> "https://tr1.api.riotgames.com";
            case LA1  -> "https://la1.api.riotgames.com";
            case LA2  -> "https://la2.api.riotgames.com";
            default   -> "https://euw1.api.riotgames.com";
        };
    }

    /** REGIONAL (account-v1, match-v5) */
    private String regionalHost(Region region) {
        return switch (region) {
            // EU klaster
            case EUW, EUNE, TR1 -> "https://europe.api.riotgames.com";
            // AM klaster
            case NA1, BR1, LA1, LA2, OC1 -> "https://americas.api.riotgames.com";
            // AS klaster
            case KR, JP1 -> "https://asia.api.riotgames.com";
            default -> "https://europe.api.riotgames.com";
        };
    }

    /* ================== ACCOUNT (Riot ID → PUUID) ================== */

    public Mono<AccountDtoRiot> getAccountByRiotId(String gameName, String tagLine, Region region) {
        String g = enc(gameName);
        String t = enc(tagLine);
        String url = regionalHost(region) + "/riot/account/v1/accounts/by-riot-id/" + g + "/" + t;
        return web.get().uri(url)
            .retrieve()
            .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("account-v1 error: " + resp.statusCode() + " " + body))))
            .bodyToMono(AccountDtoRiot.class);
    }

    public Mono<AccountDtoRiot> getAccountByPuuid(String puuid, Region region) {
        String url = regionalHost(region) + "/riot/account/v1/accounts/by-puuid/" + enc(puuid);
        return web.get().uri(url)
            .retrieve()
            .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("account-v1(by-puuid) error: " + resp.statusCode() + " " + body))))
            .bodyToMono(AccountDtoRiot.class);
    }

    /* ================== SUMMONER / LEAGUE ================== */

    public Mono<SummonerDtoRiot> getSummonerByPuuid(String puuid, Region region) {
        String url = platformHost(region) + "/lol/summoner/v4/summoners/by-puuid/" + enc(puuid);
        return web.get().uri(url)
            .retrieve()
            .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("summoner-v4 error: " + resp.statusCode() + " " + body))))
            .bodyToMono(SummonerDtoRiot.class);
    }

    public Mono<SummonerDtoRiot> getSummonerByName(String summonerName, Region region) {
        String url = platformHost(region) + "/lol/summoner/v4/summoners/by-name/" + enc(summonerName);
        return web.get().uri(url)
            .retrieve()
            .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("summoner-v4(by-name) error: " + resp.statusCode() + " " + body))))
            .bodyToMono(SummonerDtoRiot.class);
    }

    /** KORISTIMO by-puuid jer imaš pristup toj ruti */
    public Mono<List<LeagueEntryDtoRiot>> getLeagueByPuuid(String puuid, Region region) {
        String url = platformHost(region) + "/lol/league/v4/entries/by-puuid/" + enc(puuid);
        return web.get().uri(url)
            .retrieve()
            .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("league-v4(by-puuid) error: " + resp.statusCode() + " " + body))))
            .bodyToFlux(LeagueEntryDtoRiot.class)
            .collectList();
    }

    /* ================== MATCH-V5 ================== */

    public Mono<List<String>> getRecentMatchIds(String puuid, Region region, int count) {
        String url = regionalHost(region) + "/lol/match/v5/matches/by-puuid/" + enc(puuid) + "/ids?start=0&count=" + Math.max(1, count);
        return web.get().uri(url)
            .retrieve()
            .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("match-v5(ids) error: " + resp.statusCode() + " " + body))))
            .bodyToFlux(String.class)
            .collectList();
    }

public Mono<MatchV5DtoRiot> getMatch(String matchId, Region region) {
    String url = regionalHost(region) + "/lol/match/v5/matches/" + enc(matchId);
    return web.get().uri(url)
        .exchangeToMono(resp -> {
            if (resp.statusCode().is2xxSuccessful()) {
                return resp.bodyToMono(MatchV5DtoRiot.class);
            }
            if (resp.statusCode().value() == 404) {
                // "match file not found" – preskačemo ovaj meč
                return Mono.empty();
            }
            return resp.bodyToMono(String.class).flatMap(body ->
                Mono.error(new RuntimeException("match-v5(match) error: " + resp.statusCode() + " " + body))
            );
        });
}


    /* ================== helper ================== */

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
