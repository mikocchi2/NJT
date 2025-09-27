/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.dto.impl;

import com.mycompany.summoneranalyzer.entity.impl.enums.Role;

public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private Role role;

    public AuthResponse() {}
    public AuthResponse(String token, Long userId, String email, Role role){
        this.token=token; this.userId=userId; this.email=email; this.role=role;
    }
    public String getToken(){return token;} public void setToken(String token){this.token=token;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public Role getRole(){return role;} public void setRole(Role role){this.role=role;}
}