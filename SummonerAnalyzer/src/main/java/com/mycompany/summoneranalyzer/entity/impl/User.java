 
package com.mycompany.summoneranalyzer.entity.impl;

import com.mycompany.summoneranalyzer.entity.impl.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
 

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_users_email", columnNames = "email")
       })
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String email;

    @Column(nullable=false, length=255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private Role role = Role.USER;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}
    public User(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
