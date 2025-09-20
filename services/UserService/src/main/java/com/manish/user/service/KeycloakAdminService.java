package com.manish.user.service;

import com.manish.user.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KeycloakAdminService {
    @Value("${keycloak.auth-server-url}") private String keycloakUrl;
    @Value("${keycloak.realm}") private String realm;
    @Value("${keycloak.admin-username}") private String adminUser;
    @Value("${keycloak.admin-password}") private String adminPassword;
    @Value("${keycloak.client-id}") private String clientId;

    private final WebClient webClient;

    public KeycloakAdminService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String createUser(String username, String email, String password){
        String accessToken = getAdminAccessToken();

        if (accessToken == null) {
            throw new ApplicationException("Failed to create User");
        }

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("enabled", true);
        user.put("credentials", List.of(
                Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                )
        ));

        String createUserUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

        ResponseEntity<Void> response = webClient.post()
                .uri(createUserUrl)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(user), Map.class)
                .retrieve()
                .toBodilessEntity()
                .block();

        if (response == null) {
            throw new ApplicationException("Failed to create User");
        }

        String location = response.getHeaders().getFirst("Location");
        if (location == null) {
            throw new ApplicationException("User created but no Location header found");
        }
        String userId = location.substring(location.lastIndexOf("/") + 1);

        log.info("User {} created in realm {} with ID {}", username, realm, userId);
        return userId;
    }

    private String getAdminAccessToken() {
        String tokenEndpoint = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "username=" + adminUser
                                + "&password=" + adminPassword
                                + "&grant_type=password"
                                + "&client_id=" + clientId
                )
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> (String) resp.get("access_token"))
                .block();
    }
}
