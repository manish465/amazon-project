package com.manish.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String house;
    @Field("street_address_1")
    private String streetAddress1;
    @Field("street_address_2")
    private String streetAddress2;
    private String city;
    private String state;
    private String zip;
}
