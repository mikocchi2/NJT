 
package com.mycompany.summoneranalyzer.mapper.impl;

 
 
import com.mycompany.summoneranalyzer.dto.impl.MatchDto;
import com.mycompany.summoneranalyzer.entity.impl.Match;
import com.mycompany.summoneranalyzer.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper implements DtoEntityMapper<MatchDto, Match> {

    @Override
    public MatchDto toDto(Match e) {
        if (e == null) return null;
        return new MatchDto(
            e.getId(),
            e.getRegion(),
            e.getGameType(),
            e.getDurationSec(),
            e.getStartedAt()
        );
    }

    @Override
    public Match toEntity(MatchDto t) {
        Match e = new Match();
        e.setId(t.getId());
        if (t.getRegion() != null) e.setRegion(t.getRegion());
        if (t.getGameType() != null) e.setGameType(t.getGameType());
        if (t.getDurationSec() != null) e.setDurationSec(t.getDurationSec());
        if (t.getStartedAt() != null) e.setStartedAt(t.getStartedAt());
        return e;
    }
}
