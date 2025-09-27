package com.mycompany.summoneranalyzer.riot;

import com.mycompany.summoneranalyzer.entity.impl.enums.Region;
import com.mycompany.summoneranalyzer.riot.dto.LeagueEntryDtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.MatchV5DtoRiot;
import com.mycompany.summoneranalyzer.riot.dto.SummonerDtoRiot;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class RiotApiClient {
    private final WebClient web;
    public RiotApiClient(WebClient riotWebClient) { this.web = riotWebClient; }

   private String platformHost(Region region) {
    return switch (region) {
        case EUNE -> "https://eun1.api.riotgames.com";
        case EUW  -> "https://euw1.api.riotgames.com";
        case NA1  -> "https://na1.api.riotgames.com";
        case KR   -> "https://kr.api.riotgames.com";
        case BR1  -> "https://br1.api.riotgames.com";
        case JP1  -> "https://jp1.api.riotgames.com";
        case OC1  -> "https://oc1.api.riotgames.com";
        case RU   -> "https://ru.api.riotgames.com";
        case TR1  -> "https://tr1.api.riotgames.com";
        case LA1  -> "https://la1.api.riotgames.com";
        case LA2  -> "https://la2.api.riotgames.com";
    };
}

    private String regionalHost(Region region) { return "https://europe.api.riotgames.com"; }

    public Mono<SummonerDtoRiot> getSummonerByName(String name, Region region) {
        String url = platformHost(region) + "/lol/summoner/v4/summoners/by-name/" + name;
        return web.get().uri(url).retrieve().bodyToMono(SummonerDtoRiot.class);
    }

    public Mono<List<LeagueEntryDtoRiot>> getLeagueBySummonerId(String summonerId, Region region) {
        String url = platformHost(region) + "/lol/league/v4/entries/by-summoner/" + summonerId;
        return web.get().uri(url).retrieve().bodyToFlux(LeagueEntryDtoRiot.class).collectList();
    }

    public Mono<List<String>> getRecentMatchIds(String puuid, Region region, int count) {
        String url = regionalHost(region) + "/lol/match/v5/matches/by-puuid/" + puuid + "/ids?count=" + count;
        return web.get().uri(url).retrieve().bodyToFlux(String.class).collectList();
    }

    public Mono<MatchV5DtoRiot> getMatch(String matchId, Region region) {
        String url = regionalHost(region) + "/lol/match/v5/matches/" + matchId;
        return web.get().uri(url).retrieve().bodyToMono(MatchV5DtoRiot.class);
    }
}
