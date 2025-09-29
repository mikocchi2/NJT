package com.mycompany.summoneranalyzer.config;

import com.mycompany.summoneranalyzer.servis.AuthUserDetailsService;
import com.mycompany.summoneranalyzer.servis.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwt;
    private final AuthUserDetailsService users;

    // preskačemo auth rute i swagger (opciono)
    private final AntPathRequestMatcher[] skipMatchers = new AntPathRequestMatcher[] {
         new AntPathRequestMatcher("/api/**"),
            new AntPathRequestMatcher("/api/auth/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/swagger-ui.html")
    };

    public JwtAuthFilter(JwtService jwt, AuthUserDetailsService users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // preflight OPTIONS preskačemo odmah
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        for (var m : skipMatchers) if (m.matches(request)) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                String email = jwt.extractUsername(token); // baca ako je istekao/loš token
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = users.loadUserByUsername(email);
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // ne rušimo zahtev, samo beležimo i puštamo dalje — Security će vratiti 401/403 po pravilima
                log.debug("JWT filter: token invalid/expired: {}", e.getMessage());
            }
        }

        chain.doFilter(req, res);
    }
}
