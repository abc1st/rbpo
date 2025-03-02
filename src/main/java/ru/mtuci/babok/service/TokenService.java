package ru.mtuci.babok.service;

import ru.mtuci.babok.model.ApplicationUser;
import ru.mtuci.babok.model.UserSession;

import java.util.Optional;

public interface TokenService {
    public UserSession issueTokenPair(ApplicationUser user, String deviceId);
    public Optional<UserSession> refreshTokenPair(String refreshToken, String deviceId);
    public void blockAllSessionsForUser(String email);
}
