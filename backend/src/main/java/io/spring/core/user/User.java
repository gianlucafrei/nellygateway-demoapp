package io.spring.core.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {
    private String id;
    private String email;
    private String username;
    private String bio;
    private String image;
    private String loginProvider;

    public User(String id, String email, String username, String bio, String image, String loginProvider) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.bio = bio;
        this.image = image;
        this.loginProvider = loginProvider;
    }

    public User(String email, String username, String bio, String image, String loginProvider) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.username = username;
        this.bio = bio;
        this.image = image;
        this.loginProvider = loginProvider;
    }



    public void update(String email, String username, String bio, String image, String loginProvider) {
        if (!"".equals(email)) {
            this.email = email;
        }

        if (!"".equals(username)) {
            this.username = username;
        }

        if (!"".equals(bio)) {
            this.bio = bio;
        }

        if (!"".equals(image)) {
            this.image = image;
        }

        if (!"".equals(loginProvider)) {
            this.loginProvider = loginProvider;
        }
    }
}
