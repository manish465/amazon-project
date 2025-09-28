package com.manish.user.controller;

import com.manish.user.dto.SignUpResponseDTO;
import com.manish.user.dto.SignUpUserRequestDTO;
import com.manish.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class NormalUserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<SignUpResponseDTO> signUp(
            @RequestBody SignUpUserRequestDTO signUpUserRequestDTO
    ) {
        log.info("|| called signUp from NormalUserController ||");
        return new ResponseEntity<>(userService.signUp(signUpUserRequestDTO, "user"), HttpStatus.CREATED);
    }
}
