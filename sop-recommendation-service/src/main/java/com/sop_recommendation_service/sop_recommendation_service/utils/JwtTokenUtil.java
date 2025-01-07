package com.sop_recommendation_service.sop_recommendation_service.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sop_recommendation_service.sop_recommendation_service.exceptions.RecommendationException;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class JwtTokenUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Claims getAllClaimsFromToken(String token) {
        try {
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readValue(payload, Claims.class);
        } catch (Exception e) {
            throw new RecommendationException("Invalid token", e);
        }
    }
}