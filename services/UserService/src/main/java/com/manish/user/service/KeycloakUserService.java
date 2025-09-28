package com.manish.user.service;

import com.manish.user.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String createUser(
            String username,
            String email,
            String firstName,
            String lastName,
            String password,
            String role
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
        String userID = extractUserIdFromLocation(locationHeader);

        addRealmRoleToUser(userID, role);

        return userID;
    }

    public void addRealmRoleToUser(String userId, String roleName) {
        String token = getAccessToken();
        String addRoleUrl = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Get role representation first
        Map<String, Object> roleRepresentation = getRealmRole(roleName, token);
        if (roleRepresentation == null) {
            throw new ApplicationException("Realm role not found: " + roleName);
        }

        List<Map<String, Object>> roles = List.of(roleRepresentation);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(roles, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(addRoleUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Realm role '{}' added to user '{}'", roleName, userId);
            }

        } catch (HttpClientErrorException e) {
            throw new ApplicationException("Failed to add realm role to user: " + e.getResponseBodyAsString());
        }
    }

    public void addClientRoleToUser(String userId, String clientId, String roleName) {
        String token = getAccessToken();

        // Get client's internal ID
        String keycloakClientId = getClientInternalId(clientId, token);
        if (keycloakClientId == null) {
            throw new ApplicationException("Client not found: " + clientId);
        }

        String addRoleUrl = serverUrl + "/admin/realms/" + realm + "/users/" + userId +
                "/role-mappings/clients/" + keycloakClientId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Get client role representation
        Map<String, Object> roleRepresentation = getClientRole(keycloakClientId, roleName, token);
        if (roleRepresentation == null) {
            throw new ApplicationException("Client role not found: " + roleName);
        }

        List<Map<String, Object>> roles = List.of(roleRepresentation);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(roles, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(addRoleUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Client role '{}' added to user '{}'", roleName, userId);
            }

        } catch (HttpClientErrorException e) {
            throw new ApplicationException("Failed to add client role to user: " + e.getResponseBodyAsString());
        }
    }

    // Get realm role representation
    private Map<String, Object> getRealmRole(String roleName, String token) {
        String getRoleUrl = serverUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(getRoleUrl, HttpMethod.GET, entity, Map.class);
            return (Map<String, Object>) response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new ApplicationException("Failed to get realm role: " + e.getMessage());
        }
    }

    // Get client role representation
    private Map<String, Object> getClientRole(String keycloakClientId, String roleName, String token) {
        String getRoleUrl = serverUrl + "/admin/realms/" + realm + "/clients/" + keycloakClientId + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(getRoleUrl, HttpMethod.GET, entity, Map.class);
            return (Map<String, Object>) response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new ApplicationException("Failed to get client role: " + e.getMessage());
        }
    }

    // Get client internal ID by client ID
    private String getClientInternalId(String clientId, String token) {
        String getClientsUrl = serverUrl + "/admin/realms/" + realm + "/clients?clientId=" + clientId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(getClientsUrl, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> clients = response.getBody();

            if (clients != null && !clients.isEmpty()) {
                return (String) clients.get(0).get("id");
            }
        } catch (Exception e) {
            log.error("Failed to get client internal ID: {}", e.getMessage());
        }

        return null;
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
