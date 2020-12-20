package io.spring.infrastructure.service;

import com.nimbusds.jose.jwk.JWK;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWKSet;

import java.net.URL;
import java.security.Key;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Primary
@Component
public class JwtKeyResolver implements SigningKeyResolver {

    private Map<String, Key> keyMap;

    public JwtKeyResolver() throws Exception {

        String auth0Keys = "https://dev-b6-eperz.eu.auth0.com/.well-known/jwks.json";
        String googleKeys = "https://www.googleapis.com/oauth2/v3/certs";

        String[] keyUrls = new String[]{auth0Keys, googleKeys};
        loadKeys(keyUrls);
    }

    private void loadKeys(String[] keyUrls) throws Exception {

        keyMap = new HashMap<>();

        for (String url : keyUrls) {

            JWKSet publicKeys = JWKSet.load(new URL(url));

            for (JWK key : publicKeys.getKeys()) {

                String keyId = key.getKeyID();

                String keyType=key.getKeyType().getValue();
                PublicKey publicKey = key.toRSAKey().toPublicKey();

                keyMap.put(keyId, publicKey);
            }
        }
    }

    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {

        String keyId = jwsHeader.getKeyId();
        return keyMap.getOrDefault(keyId, null);
    }

    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, String s) {

        String keyId = jwsHeader.getKeyId();
        return keyMap.getOrDefault(keyId, null);
    }
}
