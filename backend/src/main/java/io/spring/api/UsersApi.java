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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private JwtTokenFilter tokenFilter;

    @Autowired
    public UsersApi(UserRepository userRepository,
                    UserQueryService userQueryService,
                    @Value("${image.default}") String defaultImage) {
        this.userRepository = userRepository;
        this.userQueryService = userQueryService;
        this.defaultImage = defaultImage;
    }

    @RequestMapping(path = "/users", method = POST)
    public ResponseEntity createUser(@Valid @RequestBody RegisterParam registerParam,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal Jwt jwt,
                                     HttpServletRequest request) {

        String id = jwt.getSubject();
        String email = jwt.getClaim("email");
        String picture = jwt.getClaim("picture");
        String issuer = jwt.getClaim("provider");

        if(picture == null || "".equals(picture))
            picture = defaultImage;

        checkInput(registerParam, bindingResult, email, id);

        User user = new User(
                id,
                email,
                registerParam.getUsername(),
            "",
                picture,
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
