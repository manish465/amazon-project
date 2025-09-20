package com.manish.user.mapper;

import com.manish.user.dto.SignUpUserRequestDTO;
import com.manish.user.model.Address;
import com.manish.user.model.User;

public class UserMapper {
    public static User toEntity(SignUpUserRequestDTO signUpUserRequestDTO) {
        Address address = Address.builder()
                .house(signUpUserRequestDTO.getAddress().getHouse())
                .streetAddress1(signUpUserRequestDTO.getAddress().getStreetAddress1())
                .streetAddress2(signUpUserRequestDTO.getAddress().getStreetAddress2())
                .city(signUpUserRequestDTO.getAddress().getCity())
                .state(signUpUserRequestDTO.getAddress().getState())
                .zip(signUpUserRequestDTO.getAddress().getZip())
                .build();

        return User.builder()
                .firstName(signUpUserRequestDTO.getUserName())
                .lastName(signUpUserRequestDTO.getLastName())
                .userName(signUpUserRequestDTO.getUserName())
                .email(signUpUserRequestDTO.getEmail())
                .country(signUpUserRequestDTO.getCountry())
                .phoneNumber(signUpUserRequestDTO.getPhoneNumber())
                .address(address)
                .build();
    }
}
