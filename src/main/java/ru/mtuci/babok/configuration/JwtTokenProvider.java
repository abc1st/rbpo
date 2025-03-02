package ru.mtuci.babok.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.accessExpiration}")
    private long accessExpiration;

    @Getter
    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(String username, Set<GrantedAuthority> authorities) {
        return createToken(username, authorities, accessExpiration, "access", null);
    }

    public String createRefreshToken(String username, Set<GrantedAuthority> authorities, String deviceId){
        return createToken(username, authorities, refreshExpiration, "refresh", deviceId);
    }

    public String createToken(String username, Set<GrantedAuthority> authorities, long expirationMillis, String tokenType, String deviceId ){
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("auth", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );

        claims.put("token_type", tokenType);
        if (deviceId != null){
            claims.put("deviceId", deviceId);
        }

        Date now = new Date();
        Date expiationDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getTokenType(String token){
        Claims claims = Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token).getBody();
        return claims.get("token_type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

    public Authentication getAuthentication(String token) {
        String login = getUsername(token);
        UserDetails user = userDetailsService.loadUserByUsername(login);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

}
