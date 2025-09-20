package com.manish.user.service;

import com.manish.user.UserRepository;
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
    private final KeycloakAdminService keycloakAdminService;
    private final UserRepository userRepository;

    public SignUpResponseDTO signUp(SignUpUserRequestDTO signUpUserRequestDTO) {
        String authID = keycloakAdminService.createUser(
                signUpUserRequestDTO.getUserName(),
                signUpUserRequestDTO.getEmail(),
                signUpUserRequestDTO.getPassword()
        );

        User user = UserMapper.toEntity(signUpUserRequestDTO);
        user.setAuthID(authID);

        String userID = userRepository.save(user).getId();

        return new SignUpResponseDTO("User Created", userID, authID);
    }
}
