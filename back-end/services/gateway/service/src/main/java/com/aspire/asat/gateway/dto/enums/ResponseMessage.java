package com.aspire.asat.gateway.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseMessage {
    OPERATION_SUCCESSFUL("operation.successful"),
    RECORD_NOT_FOUND("record.not.found"),
    DATABASE_EXCEPTION("database.exception"),
    RECORD_ALREADY_EXIST("record.already.exist"),
    INVALID_REQUEST_DATA("invalid.request.data"),
    LOCALE_RECORD_NOT_FOUND("locale.record.not.found"),
    UNAUTHORIZED_RESOURCE_ACCESS("unauthorized.resource.access"),
    INTER_SERVICE_COMMUNICATION_ERROR("inter.service.communication.error"),

    // SIGN UP
    INVALID_STEP("invalid.step"),
    ACCOUNT_ALREADY_EXIST("account.already.exist"),
    USERNAME_ALREADY_EXIST("username.already.exist"),
    PHONE_ALREADY_EXIST("phone.already.exist"),
    SESSION_ALREADY_EXPIRED("session.already.expired"),
    CARD_INFORMATION_NOT_MATCH("card.information.not.match"),
    ACCOUNT_INFORMATION_NOT_MATCH("account.information.not.match"),
    PASSWORD_POLICY_NOT_MATCH("password.policy.not.match.with.system.requirement"),
    USER_NOT_FOUND("user.not.found"),
    USER_IS_NOT_ACTIVE("user.is.not.active"),


    USERNAME_PASSWORD_NOT_MATCH("username.password.not.match"),;


    private final String responseMessage;
}
