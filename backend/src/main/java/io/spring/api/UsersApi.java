package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.jsonwebtoken.Claims;
import io.spring.api.exception.InvalidRequestException;
import io.spring.api.security.JwtTokenFilter;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.user.EncryptService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class UsersApi {
    private UserRepository userRepository;
    private UserQueryService userQueryService;
    private String defaultImage;
    private EncryptService encryptService;
    private JwtTokenFilter tokenFilter;

    @Autowired
    public UsersApi(UserRepository userRepository,
                    UserQueryService userQueryService,
                    EncryptService encryptService,
                    @Value("${image.default}") String defaultImage,
                    JwtTokenFilter tokenFilter) {
        this.userRepository = userRepository;
        this.userQueryService = userQueryService;
        this.encryptService = encryptService;
        this.defaultImage = defaultImage;
        this.tokenFilter = tokenFilter;
    }

    @RequestMapping(path = "/users", method = POST)
    public ResponseEntity createUser(@Valid @RequestBody RegisterParam registerParam, BindingResult bindingResult, HttpServletRequest request) {
        Optional<Claims> validatedClaimsFromRequest = tokenFilter.getValidatedClaimsFromRequest(request);

        if(!validatedClaimsFromRequest.isPresent())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Claims claims = validatedClaimsFromRequest.get();

        String id = claims.getSubject();
        String email = claims.get("email", String.class);
        String picture = claims.get("picture", String.class);
        String issuer = claims.getIssuer();


        checkInput(registerParam, bindingResult, email, id);

        User user = new User(
                id,
                email,
                registerParam.getUsername(),
            "",
                picture != null ? picture : defaultImage,
                issuer);
        userRepository.save(user);

        UserData userData = userQueryService.findById(user.getId()).get();
        return ResponseEntity.status(201).body(userResponse(userData));
    }

    private void checkInput(@Valid @RequestBody RegisterParam registerParam, BindingResult bindingResult, String email, String id) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }

        if (userRepository.findById(id).isPresent()){
            bindingResult.rejectValue("username", "DUPLICATED", "already registered");
        }

        if (userRepository.findByEmail(email).isPresent()){
            bindingResult.rejectValue("username", "DUPLICATED", "already registered with another provider");
        }

        if (userRepository.findByUsername(registerParam.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "DUPLICATED", "duplicated username");
        }

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
    }

    private Map<String, Object> userResponse(UserData userWithToken) {
        return new HashMap<String, Object>() {{
            put("user", userWithToken);
        }};
    }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class RegisterParam {
    @NotBlank(message = "can't be empty")
    private String username;
}
