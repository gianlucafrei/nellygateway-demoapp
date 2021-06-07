package io.spring.api.security;

import io.jsonwebtoken.Claims;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.service.DefaultJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Profile("never")
@SuppressWarnings("SpringJavaAutowiringInspection")
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DefaultJwtService jwtService;

    @Value("${jwt.header}")
    private String header;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Optional<Claims> claimsOptional = getValidatedClaimsFromRequest(request);

        if(claimsOptional.isPresent()){

            Claims claims = claimsOptional.get();
            String id = claims.getSubject();

            userRepository.findById(id).ifPresent(user -> {

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.emptyList()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            });
        }
        else {

            if(request.getHeader(header) != null){
                response.addHeader("WWW-Authenticate", "Bearer Error: Invalid JWT");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT");
            }
        }

        filterChain.doFilter(request, response);
    }

    public Optional<Claims> getValidatedClaimsFromRequest(HttpServletRequest request){

        Optional<String> tokenString = getTokenString(request.getHeader(header));

        if(tokenString.isPresent()){
            return jwtService.getClaimsFromToken(tokenString.get());
        }

        return Optional.empty();
    }

    private Optional<String> getTokenString(String header) {
        if (header == null) {
            return Optional.empty();
        } else {
            String[] split = header.split(" ");
            if (split.length < 2) {
                return Optional.ofNullable(header);
            } else {
                return Optional.ofNullable(split[1]);
            }
        }
    }
}

