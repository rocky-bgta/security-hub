package com.aspire.asat.registration.exception;

import com.aspire.asat.registration.data.trial.response.ExistingUsersResponseDto;
import lombok.Getter;

@Getter
public class DomainConflictException extends RuntimeException {
    private final ExistingUsersResponseDto existingUsersInfo;

    public DomainConflictException(String message, ExistingUsersResponseDto existingUsersInfo) {
        super(message);
        this.existingUsersInfo = existingUsersInfo;
    }
}
