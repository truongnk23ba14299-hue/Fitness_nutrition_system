package com.fitness.service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {
    private final GoogleIdTokenVerifier verifier;
    private final String googleClientId;

    public GoogleAuthService(@Value("${app.google.client-id}") String googleClientId) {
        this.googleClientId = googleClientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())

                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleIdToken.Payload verifyGoogleToken(String idToken)  {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

        GoogleIdToken.Payload payload = token.getPayload();
        if (!googleClientId.equals(payload.getAudience())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google Client Id");
        }
        if (!Boolean.TRUE.equals(payload.getEmailVerified())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email has not been verified");
        }
        return payload;
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google Token verification failed");
        }
    }
}


