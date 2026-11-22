package nl.example.tokenservice;

import io.cucumber.java.nl.Als;
import io.cucumber.java.nl.Dan;
import io.cucumber.java.nl.En;
import io.cucumber.java.nl.Gegeven;
import io.jsonwebtoken.JwtException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TokenStepDefinitions {

    private static final String SECRET = "dit-is-een-demo-geheim-van-minstens-32-bytes!";

    private TokenService tokenService;
    private String token;

    @Gegeven("een tokenservice met een geldig geheim")
    public void aTokenServiceWithAValidSecret() {
        tokenService = new TokenService(SECRET, Duration.ofMinutes(15));
    }

    @Als("er een token wordt uitgegeven voor {string}")
    @En("er is een token uitgegeven voor {string}")
    public void aTokenIsIssuedFor(String subject) {
        token = tokenService.issue(subject);
    }

    @Als("iemand het token aanpast")
    public void someoneTampersWithTheToken() {
        token = token.substring(0, token.length() - 2) + "xx";
    }

    @Dan("hoort het token bij {string}")
    public void theTokenBelongsTo(String subject) {
        assertThat(tokenService.subjectOf(token)).isEqualTo(subject);
    }

    @Dan("wordt het token geweigerd")
    public void theTokenIsRejected() {
        assertThatThrownBy(() -> tokenService.subjectOf(token)).isInstanceOf(JwtException.class);
    }
}
