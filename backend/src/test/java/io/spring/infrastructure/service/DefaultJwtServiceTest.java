package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultJwtServiceTest {

    private JwtService jwtService;

    @Before
    public void setUp() {
        jwtService = new DefaultJwtService("deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef");
    }

    @Test
    public void should_get_null_with_wrong_jwt() {
        assertFalse(jwtService.getClaimsFromToken("123").isPresent());
    }

    @Test
    public void hmac_test(){

        String token = "eyJraWQiOiJLZXlJRCIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJzdWIiOiJKb2huIERvZSJ9.80NhW_8MTDRsWfOHNO7fy_tlfUXL-AmCXIAUrSh1n5k";

        Optional<Claims> claimsOptional = jwtService.getClaimsFromToken(token);

        assertTrue(claimsOptional.isPresent());
        assertEquals("John Doe", claimsOptional.get().getSubject());
    }

    @Test
    public void hmac_test_expired(){

        String token = "eyJraWQiOiJLZXlJRCIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJzdWIiOiJKb2huIERvZSIsImV4cCI6MH0.dECQSZGANFWuGVEo0BTWMMjQReYYo2fIOVyFVhRKCmo";

        Optional<Claims> claimsOptional = jwtService.getClaimsFromToken(token);

        assertFalse(claimsOptional.isPresent());
    }

    @Test
    public void hmac_test_wrongKey(){

        String token = "eyJraWQiOiJLZXlJRCIsInR5cCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJzdWIiOiJKb2huIERvZSJ9.80NhW_8MTDRsWfOHNO7fy_tlfUXL-AmCXIAUrSh1n5k";
        String key = "00000000deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef";

        JwtService jwtService = new DefaultJwtService(key);


        Optional<Claims> claimsOptional = jwtService.getClaimsFromToken(token);

        assertFalse(claimsOptional.isPresent());
    }
}