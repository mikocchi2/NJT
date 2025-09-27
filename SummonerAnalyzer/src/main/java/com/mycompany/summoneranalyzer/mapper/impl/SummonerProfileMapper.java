
package com.mycompany.summoneranalyzer.mapper.impl;
 
 
import com.mycompany.summoneranalyzer.dto.impl.SummonerProfileDto;
import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class SummonerProfileMapper implements DtoEntityMapper<SummonerProfileDto, SummonerProfile> {

    @Override
    public SummonerProfileDto toDto(SummonerProfile e) {
        if (e == null) return null;
        return new SummonerProfileDto(
            e.getId(),
            e.getPuuid(),
            e.getName(),
            e.getRegion(),
            e.getLevel(),
            e.getRankTier(),
            e.getRankDivision(),
            e.getLeaguePoints(),
            e.getLastSyncedAt()
        );
    }

    @Override
    public SummonerProfile toEntity(SummonerProfileDto t) {
        SummonerProfile e = new SummonerProfile();
        e.setId(t.getId());
        e.setPuuid(t.getPuuid());
        e.setName(t.getName());
        if (t.getRegion() != null) e.setRegion(t.getRegion());
        if (t.getLevel() != null) e.setLevel(t.getLevel());
        e.setRankTier(t.getRankTier());
        e.setRankDivision(t.getRankDivision());
        e.setLeaguePoints(t.getLeaguePoints());
        if (t.getLastSyncedAt() != null) e.setLastSyncedAt(t.getLastSyncedAt());
        return e;
    }
}
