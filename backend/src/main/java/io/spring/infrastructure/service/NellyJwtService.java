package io.spring.infrastructure.service;

import io.jsonwebtoken.*;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Optional;

@Primary
@Component
public class NellyJwtService implements JwtService {

    private SigningKeyResolver keyResolver;

    @Autowired
    public NellyJwtService(@Autowired SigningKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    @Override
    public String toToken(User user) {

        throw new NotImplementedException();
    }

    public Optional<Claims> getClaimsFromToken(String token){

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKeyResolver(keyResolver)
                    .parseClaimsJws(token);
            return Optional.ofNullable(claimsJws.getBody());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getSubFromToken(String token) {

        Optional<Claims> claims = getClaimsFromToken(token);
        return claims.isPresent() ? Optional.of(claims.get().getSubject()) : Optional.empty();
    }
}
