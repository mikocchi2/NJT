/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.dto.impl;
 

import com.mycompany.summoneranalyzer.entity.impl.enums.Role;
import jakarta.validation.constraints.*;

public class RegisterRequest {
    @Email @NotBlank private String email;
    @NotBlank @Size(min=6) private String password;
    private Role role = Role.USER; // dozvoli izbor ili uvek USER

    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getPassword(){return password;} public void setPassword(String password){this.password=password;}
    public Role getRole(){return role;} public void setRole(Role role){this.role=role;}
}

