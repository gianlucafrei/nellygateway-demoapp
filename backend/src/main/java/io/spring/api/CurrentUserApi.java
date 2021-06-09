package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.jsonwebtoken.Claims;
import io.spring.api.exception.InvalidRequestException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/user")
public class CurrentUserApi {
    private UserQueryService userQueryService;
    private UserRepository userRepository;
    private String defaultImage;

    @Autowired
    public CurrentUserApi(UserQueryService userQueryService, UserRepository userRepository, @Value("${image.default}") String defaultImage) {
        this.userQueryService = userQueryService;
        this.userRepository = userRepository;
        this.defaultImage = defaultImage;
    }

    @GetMapping
    public ResponseEntity currentUser(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(jwt == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("NOT-REGISTERED")))
        {
            return ResponseEntity.ok(new HashMap<String, Object>() {{

                String picture = jwt.getClaim("picture");
                if(picture == null || "".equals(picture))
                    picture = defaultImage;

                put("email", jwt.getClaim("email"));
                put("needsRegistration", true);
                put("picture", picture);
            }});
        }else {

            UserData userData = userQueryService.findById(auth.getName()).get();
            return ResponseEntity.ok(userResponse(userData));
        }
    }

    @PutMapping
    public ResponseEntity updateProfile(@AuthenticationPrincipal User currentUser,
                                        @Valid @RequestBody UpdateUserParam updateUserParam,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
        checkUniquenessOfUsername(currentUser, updateUserParam, bindingResult);

        currentUser.update(
                updateUserParam.getEmail(),
                updateUserParam.getUsername(),
                updateUserParam.getBio(),
                updateUserParam.getImage(),
                "");
        userRepository.save(currentUser);
        UserData userData = userQueryService.findById(currentUser.getId()).get();
        return ResponseEntity.ok(userResponse(userData));
    }

    private void checkUniquenessOfUsername(User currentUser, UpdateUserParam updateUserParam, BindingResult bindingResult) {
        if (!"".equals(updateUserParam.getUsername())) {
            Optional<User> byUsername = userRepository.findByUsername(updateUserParam.getUsername());
            if (byUsername.isPresent() && !byUsername.get().equals(currentUser)) {
                bindingResult.rejectValue("username", "DUPLICATED", "username already exist");
            }
        }

        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult);
        }
    }

    private Map<String, Object> userResponse(UserData user) {
        return new HashMap<String, Object>() {{
            put("user", user);
        }};
    }
}

@Getter
@JsonRootName("user")
@NoArgsConstructor
class UpdateUserParam {
    @Email(message = "should be an email")
    private String email = "";
    private String username = "";
    private String bio = "";
    private String image = "";
}
