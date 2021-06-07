package io.spring.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;

import java.util.Collections;
import java.util.Map;

public class CustomClaimsDecoder implements
        Converter<Map<String, Object>, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(CustomClaimsDecoder.class);

    private final MappedJwtClaimSetConverter delegate =
            MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

    public Map<String, Object> convert(Map<String, Object> claims) {

        Map<String, Object> convertedClaims = this.delegate.convert(claims);

        // Own logic here
        log.debug("Claims: {}, Converted: {}", claims, convertedClaims);

        return convertedClaims;
    }
}