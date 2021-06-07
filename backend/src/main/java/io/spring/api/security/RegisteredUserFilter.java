package io.spring.api.security;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class RegisteredUserFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RegisteredUserFilter.class);

    public RegisteredUserFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication instanceof JwtAuthenticationToken)
        {
            // Get authentication object
            JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
            ArrayList<GrantedAuthority> updatedAuthorities = new ArrayList<>(jwtAuthentication.getAuthorities());
            Jwt principal = jwtAuthentication.getToken();

            // Add REGISTERED authority if the user has already registered themself
            String id = authentication.getName();
            Optional<User> userOptional = userRepository.findById(id);
            if(userOptional.isPresent()){
                updatedAuthorities.add(new SimpleGrantedAuthority("REGISTERED"));
                principal = new JwtWithUser(principal, userOptional.get());
            }else {
                updatedAuthorities.add(new SimpleGrantedAuthority("NOT-REGISTERED"));
            }

            // Safe updated authentication
            JwtAuthenticationToken updatedAuthentication = new JwtAuthenticationToken(principal, updatedAuthorities, jwtAuthentication.getName());
            SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
        }
        chain.doFilter(request,  response);
    }
}
