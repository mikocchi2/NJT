 
package com.mycompany.summoneranalyzer.entity.impl;
 

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite",
       uniqueConstraints = @UniqueConstraint(name="uk_fav_user_summoner", columnNames = {"user_id","summoner_id"}),
       indexes = {
           @Index(name="ix_fav_user", columnList = "user_id"),
           @Index(name="ix_fav_summoner", columnList = "summoner_id")
       })
public class Favorite {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="summoner_id", nullable=false)
    private SummonerProfile summoner;

    @Column(nullable=true, length=140)
    private String note;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Favorite() {}
    public Favorite(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public SummonerProfile getSummoner() { return summoner; }
    public void setSummoner(SummonerProfile summoner) { this.summoner = summoner; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
