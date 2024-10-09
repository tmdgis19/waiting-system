package com.personal.waiting_api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {
    private HttpStatus httpStatus;
    private String code;
    private String reason;

}
