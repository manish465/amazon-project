package com.manish.user.service;

import com.manish.user.repository.UserRepository;
import com.manish.user.dto.SignUpResponseDTO;
import com.manish.user.dto.SignUpUserRequestDTO;
import com.manish.user.mapper.UserMapper;
import com.manish.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final KeycloakUserService keycloakAdminService;
    private final UserRepository userRepository;

    public SignUpResponseDTO signUp(SignUpUserRequestDTO signUpUserRequestDTO) {
        String keycloakUserId = keycloakAdminService.createUser(
                signUpUserRequestDTO.getUserName(),
                signUpUserRequestDTO.getEmail(),
                signUpUserRequestDTO.getFirstName(),
                signUpUserRequestDTO.getLastName(),
                signUpUserRequestDTO.getPassword()
        );

        User user = UserMapper.toEntity(signUpUserRequestDTO);
        user.setAuthID(keycloakUserId);

        String userID = userRepository.save(user).getId();

        return new SignUpResponseDTO("User Created", userID, keycloakUserId);
    }
}
