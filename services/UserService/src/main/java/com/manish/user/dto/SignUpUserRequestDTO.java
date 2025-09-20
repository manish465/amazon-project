package com.manish.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpUserRequestDTO {
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String password;
    private String authID;
    private String country;
    private String phoneNumber;
    private SignUpAddressRequestDTO address;
}
