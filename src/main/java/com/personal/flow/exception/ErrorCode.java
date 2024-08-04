package com.personal.flow.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum ErrorCode {

    QUEUE_ALREADY_REGISTERED_USER(HttpStatus.CONFLICT, "UQ-0001", "Already registered in queue");


    private HttpStatus httpStatus;
    private String code;
    private String reason;

    public ApplicationException build(){
        return new ApplicationException(httpStatus, code, reason);
    }

    public ApplicationException build(Object... args){
        return new ApplicationException(httpStatus, code, reason.formatted(args));
    }

}
