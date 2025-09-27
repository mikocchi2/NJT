 
package com.mycompany.summoneranalyzer.mapper.impl;
 
 
import com.mycompany.summoneranalyzer.dto.impl.MatchSummaryDto;
import com.mycompany.summoneranalyzer.entity.impl.Match;
import com.mycompany.summoneranalyzer.entity.impl.MatchSummary;
import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class MatchSummaryMapper implements DtoEntityMapper<MatchSummaryDto, MatchSummary> {

    @Override
    public MatchSummaryDto toDto(MatchSummary e) {
        if (e == null) return null;
        return new MatchSummaryDto(
            e.getId(),
            e.getMatch() != null ? e.getMatch().getId() : null,
            e.getSummoner() != null ? e.getSummoner().getId() : null,
            e.getChampion(),
            e.getKills(),
            e.getDeaths(),
            e.getAssists(),
            e.isWin()
        );
    }

    @Override
    public MatchSummary toEntity(MatchSummaryDto t) {
        MatchSummary e = new MatchSummary();
        e.setId(t.getId());
        if (t.getMatchId() != null) e.setMatch(new Match(t.getMatchId()));
        if (t.getSummonerId() != null) e.setSummoner(new SummonerProfile(t.getSummonerId()));
        e.setChampion(t.getChampion());
        if (t.getKills() != null) e.setKills(t.getKills());
        if (t.getDeaths() != null) e.setDeaths(t.getDeaths());
        if (t.getAssists() != null) e.setAssists(t.getAssists());
        if (t.getWin() != null) e.setWin(t.getWin());
        return e;
    }
}
