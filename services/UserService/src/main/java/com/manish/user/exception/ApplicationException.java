package com.manish.user.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message){
        super(message);
    }
}
