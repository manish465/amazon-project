package com.manish.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Field(name = "userID")
    private String id;
    @Field(name = "first_name")
    private String firstName;
    @Field(name = "last_name")
    private String lastName;
    @Field(name = "user_name")
    private String userName;
    private String email;
    @Field(name = "authID")
    private String authID;
    private String country;
    @Field(name = "phone_number")
    private String phoneNumber;
    private Address address;
    @Field(name = "created_at")
    private LocalDateTime createdAt;
    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
}
