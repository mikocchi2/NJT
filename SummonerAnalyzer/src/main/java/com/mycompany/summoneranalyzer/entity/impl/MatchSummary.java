 
package com.mycompany.summoneranalyzer.entity.impl;
 

import jakarta.persistence.*;

@Entity
@Table(name = "match_summary",
       indexes = {
           @Index(name="ix_ms_summoner", columnList = "summoner_id"),
           @Index(name="ix_ms_match", columnList = "match_id")
       })
public class MatchSummary {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="match_id", nullable=false,
        foreignKey = @ForeignKey(name="fk_ms_match"))
    private Match match; // ← veza rešava identifikaciju, nema zasebnog matchId polja

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="summoner_id", nullable=false,
        foreignKey = @ForeignKey(name="fk_ms_summoner"))
    private SummonerProfile summoner;

    @Column(nullable=false, length=40)
    private String champion;

    @Column(nullable=false) private int kills;
    @Column(nullable=false) private int deaths;
    @Column(nullable=false) private int assists;

    @Column(nullable=false)
    private boolean win;

    public MatchSummary() {}
    public MatchSummary(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public SummonerProfile getSummoner() { return summoner; }
    public void setSummoner(SummonerProfile summoner) { this.summoner = summoner; }

    public String getChampion() { return champion; }
    public void setChampion(String champion) { this.champion = champion; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }

    public boolean isWin() { return win; }
    public void setWin(boolean win) { this.win = win; }
}
