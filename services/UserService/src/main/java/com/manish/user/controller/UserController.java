package com.manish.user.controller;

import com.manish.user.dto.SignUpResponseDTO;
import com.manish.user.dto.SignUpUserRequestDTO;
import com.manish.user.service.UserService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/get")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello World", HttpStatus.OK);
    }

    @PermitAll
    @PreAuthorize("permitAll()")
    public ResponseEntity<SignUpResponseDTO> signUp(
            @RequestBody SignUpUserRequestDTO signUpUserRequestDTO
    ) {
        log.info("|| called signUp from UserController ||");
        return new ResponseEntity<>(userService.signUp(signUpUserRequestDTO), HttpStatus.CREATED);
    }
}
