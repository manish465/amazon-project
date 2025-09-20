package com.manish.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpAddressRequestDTO {
    private String house;
    private String streetAddress1;
    private String streetAddress2;
    private String city;
    private String state;
    private String zip;
}
