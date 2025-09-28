package com.manish.user.service;

import com.manish.user.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService {
    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createUser(
            String username,
            String email,
            String firstName,
            String lastName,
            String password
    ) {
        String token = getAccessToken();
        String createUserUrl = serverUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userRepresentation = new HashMap<>();
        userRepresentation.put("username", username);
        userRepresentation.put("email", email);
        userRepresentation.put("firstName", firstName);
        userRepresentation.put("lastName", lastName);
        userRepresentation.put("enabled", true);
        userRepresentation.put("emailVerified", true);

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", password);
        credential.put("temporary", false);
        userRepresentation.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userRepresentation, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(createUserUrl, request, String.class);

        if(response.getHeaders().getFirst("Location") == null) {
            log.error("Failed to create user in keycloak");
            throw new ApplicationException("Failed to create user in keycloak");
        }

        String locationHeader = response.getHeaders().getFirst("Location");

        return extractUserIdFromLocation(locationHeader);
    }

    private String getAccessToken(){
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if(response.getBody() == null) {
            log.error("Failed to get access token");
            throw new ApplicationException("Authentication failed");
        }

        return (String) response.getBody().get("access_token");
    }

    private String extractUserIdFromLocation(String locationHeader) {
        if (locationHeader != null && locationHeader.contains("/users/")) {
            String[] parts = locationHeader.split("/users/");
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return null;
    }
}
