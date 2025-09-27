/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.riot;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.*;

@Configuration
public class RiotApiConfig {

    @Bean
    public WebClient riotWebClient(@Value("${riot.api.key}") String apiKey) {
        return WebClient.builder()
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }
}
