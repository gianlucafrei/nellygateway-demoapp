package io.spring.core.service;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface JwtService {

    Optional<Claims> getClaimsFromToken(String token);
}
