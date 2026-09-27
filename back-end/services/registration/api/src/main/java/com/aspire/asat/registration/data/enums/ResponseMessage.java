package com.aspire.asat.registration.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseMessage {

    OPERATION_SUCCESSFUL("operation.success"),
    RECORD_NOT_FOUND("record.not.found"),
    OPERATION_ALREADY_PERFORMED("operation.already.performed"),
    LOCALE_RECORD_NOT_FOUND("locale.record.not.found"),
    INTER_SERVICE_COMMUNICATION_ERROR("inter.service.communication.exception"),
    INTERNAL_SERVICE_EXCEPTION("internal.service.exception"),
    DATABASE_EXCEPTION("database.exception"),
    TEMPLATE_PARAM_COUNT_MISMATCH("template.param.count.mismatch"),
    TEMPLATE_PARAM_MISMATCH("template.param.mismatch"),
    TEMPLATE_PROCESSING_ERROR("template.processing.error"),
    INVALID_REQUEST_DATA("invalid.request.data"),
    INVALID_REQUEST_METHOD_TYPE("invalid.request.method.type"),
    SMS_DATA_EXCEPTION("invalid.sms.data"),
    TEMPLATE_ACTIVENESS_ERROR("invalid.request.data"),
    TEMPLATE_PARAM_TYPO("template.param.typo"),
    JSON_PARSE_ERROR("json.parse.error"),
    RECORD_ALREADY_EXIST("record.already.exist"),
    UNAUTHORIZED_RESOURCE_ACCESS("unauthorized.resource.access"),
    ROLE_UPDATE_FAILED("role.update.failed"),
    ROLE_DELETE_FAILED("role.delete.failed"),
    ROLE_CREATE_FAILED("role.create.failed"),
    ROLE_NOT_FOUND("role.not.found"),
    NOT_ALLOWED_OPERATION("not.allowed.operation"),
    CASE_ALREADY_COMPLETED("case.already.completed"),
    FAIL_TO_GET_ACCOUNT_INFO("fail.to.get.account.info"),
    CUSTOMER_INFO_NOT_FOUND("customer.info.not.found"),
    CANCELLED_MESSAGE("Application cancelled successfully"),
    LOCKED_MESSAGE("Application Locked successfully"),
    RETURNED_MESSAGE("Application Returned successfully"),
    APPROVED_MESSAGE("Application approved successfully"),
    STATUS_UPDATE_MESSAGE("Application Status updated successfully"),
    FETCHED_SUCCESS("Menu successfully"),
    MENU_UPDATED("Menu  updated successfully"),
    MENU_DELETED("Menu updated successfully"),
    MENU_CREATED("Success");



    private final String responseMessage;
}
