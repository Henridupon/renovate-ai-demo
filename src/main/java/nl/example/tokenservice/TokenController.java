package nl.example.tokenservice;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public TokenResponse issue(@RequestBody IssueRequest request) {
        return new TokenResponse(tokenService.issue(request.subject()));
    }

    @PostMapping("/verify")
    public SubjectResponse verify(@RequestBody VerifyRequest request) {
        return new SubjectResponse(tokenService.subjectOf(request.token()));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Void> invalidToken() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public record IssueRequest(String subject) {
    }

    public record VerifyRequest(String token) {
    }

    public record TokenResponse(String token) {
    }

    public record SubjectResponse(String subject) {
    }
}
