 
package com.mycompany.summoneranalyzer.mapper.impl;
 
 
import com.mycompany.summoneranalyzer.dto.impl.FavoriteDto;
import com.mycompany.summoneranalyzer.entity.impl.Favorite;
import com.mycompany.summoneranalyzer.entity.impl.SummonerProfile;
import com.mycompany.summoneranalyzer.entity.impl.User;
import com.mycompany.summoneranalyzer.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper implements DtoEntityMapper<FavoriteDto, Favorite> {

    @Override
    public FavoriteDto toDto(Favorite e) {
        if (e == null) return null;
        return new FavoriteDto(
            e.getId(),
            e.getUser() != null ? e.getUser().getId() : null,
            e.getSummoner() != null ? e.getSummoner().getId() : null,
            e.getNote(),
            e.getCreatedAt()
        );
    }

    @Override
    public Favorite toEntity(FavoriteDto t) {
        Favorite e = new Favorite();
        e.setId(t.getId());
        if (t.getUserId() != null) e.setUser(new User(t.getUserId()));
        if (t.getSummonerId() != null) e.setSummoner(new SummonerProfile(t.getSummonerId()));
        e.setNote(t.getNote());
        if (t.getCreatedAt() != null) e.setCreatedAt(t.getCreatedAt());
        return e;
    }
}
