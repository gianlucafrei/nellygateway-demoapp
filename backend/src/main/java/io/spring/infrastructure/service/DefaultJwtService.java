package io.spring.infrastructure.service;

import io.jsonwebtoken.*;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

@Component
public class DefaultJwtService implements JwtService {

    private JwtParser parser;

    @Autowired
    public DefaultJwtService(@Value("${jwt.secret}") String secret) {

        byte[] bytes = Hex.decode(secret);

        SecretKey key =  new SecretKeySpec(bytes, 0, bytes.length, "HMAC");

        this.parser = Jwts.parser().setSigningKey(key);
    }

    @Override
    public Optional<Claims> getClaimsFromToken(String token) {


        try {
            Jws<Claims> claimsJws = parser.parseClaimsJws(token);
            return Optional.ofNullable(claimsJws.getBody());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
