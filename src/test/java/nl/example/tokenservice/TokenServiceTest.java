package nl.example.tokenservice;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "dit-is-een-demo-geheim-van-minstens-32-bytes!";
    private static final String OTHER_SECRET = "een-ander-geheim-van-minstens-32-bytes-lang";

    private final TokenService tokenService = new TokenService(SECRET, Duration.ofMinutes(15));

    @Test
    void issuedTokenResolvesToItsSubject() {
        String token = tokenService.issue("alice");

        assertThat(tokenService.subjectOf(token)).isEqualTo("alice");
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = tokenService.issue("alice");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> tokenService.subjectOf(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithOtherKeyIsRejected() {
        String token = new TokenService(OTHER_SECRET, Duration.ofMinutes(15)).issue("alice");

        assertThatThrownBy(() -> tokenService.subjectOf(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = new TokenService(SECRET, Duration.ofSeconds(-5)).issue("bob");

        assertThatThrownBy(() -> tokenService.subjectOf(token)).isInstanceOf(JwtException.class);
    }
}
