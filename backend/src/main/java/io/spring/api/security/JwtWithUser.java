package io.spring.api.security;

import io.spring.core.user.User;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtWithUser extends Jwt {

    private User user;

    public JwtWithUser(Jwt token, User user) {
        super(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt(), token.getHeaders(), token.getClaims());
        this.user = user;
    }

    public User getCurrentUser() {
        return user;
    }

    public String getId(){
        return user.getId();
    }
}
