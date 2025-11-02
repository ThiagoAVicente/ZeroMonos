package org.hw1.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidStatusTransitionException extends Exception {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}