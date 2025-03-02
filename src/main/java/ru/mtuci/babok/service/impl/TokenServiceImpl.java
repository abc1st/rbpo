package ru.mtuci.babok.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mtuci.babok.configuration.JwtTokenProvider;
import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.SessionStatus;
import ru.mtuci.babok.model.UserSession;
import ru.mtuci.babok.repository.UserRepository;
import ru.mtuci.babok.repository.UserSessionRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserSession issueTokenPair(ApplicationUser user, String deviceId) {
        Set<GrantedAuthority> grantedAuthorities = user.getRole().getGrantedAuthorities();
        String accessToken = jwtTokenProvider.createAccessToken(user.getLogin(), grantedAuthorities);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getLogin(), grantedAuthorities, deviceId);
        UserSession session = new UserSession();
        session.setEmail(user.getEmail());
        session.setDeviceId(deviceId);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setAccessTokenExpiration(new Date(System.currentTimeMillis() + jwtTokenProvider.getAccessExpiration()));
        session.setRefreshTokenExpiration(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()));
        session.setStatus(SessionStatus.ACTIVE);

        return userSessionRepository.save(session);
    }

    @Transactional
    public Optional<UserSession> refreshTokenPair(String refreshToken, String deviceId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        if (!sessionOpt.isPresent()) return Optional.empty();

        UserSession session = sessionOpt.get();
        if (!session.getDeviceId().equals(deviceId) || session.getStatus() != SessionStatus.ACTIVE ||
                session.getRefreshTokenExpiration().before(new Date())) {
            blockAllSessionsForUser(session.getEmail());
            return Optional.empty();
        }

        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        ApplicationUser user = userRepository.findByEmail(session.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        Set<GrantedAuthority> grantedAuthorities = user.getRole().getGrantedAuthorities();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getLogin(), grantedAuthorities);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getLogin(), grantedAuthorities, deviceId);
        UserSession newSession = new UserSession();
        newSession.setEmail(user.getEmail());
        newSession.setDeviceId(deviceId);
        newSession.setAccessToken(newAccessToken);
        newSession.setRefreshToken(newRefreshToken);
        newSession.setAccessTokenExpiration(new Date(System.currentTimeMillis() + jwtTokenProvider.getAccessExpiration()));
        newSession.setRefreshTokenExpiration(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()));
        newSession.setStatus(SessionStatus.ACTIVE);

        return Optional.of(userSessionRepository.save(newSession));
    }

    @Transactional
    public void blockAllSessionsForUser(String email) {
        List<UserSession> sessions = userSessionRepository.findByEmail(email);
        sessions.forEach(session -> {
            session.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(session);
        });
    }
}