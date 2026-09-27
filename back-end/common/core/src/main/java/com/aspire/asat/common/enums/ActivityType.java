package com.aspire.asat.common.enums;

public enum ActivityType {
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_STATUS_CHANGED,
    USER_LOGIN,
    USER_LOGOUT,
    PASSWORD_CHANGED,
    PASSWORD_RESET,
    /** User requested password reset (forgot password) - reset link sent to email */
    PASSWORD_RESET_REQUESTED,
    /** Set password token/link generated for user (e.g. new user or admin-triggered set password) */
    PASSWORD_SET_TOKEN_GENERATED,
    LICENSE_ALLOCATED,
    LICENSE_DEALLOCATED,
    LICENSE_TRANSFERRED,
    MSP_CREATED,
    MSP_UPDATED,
    MSP_STATUS_CHANGED,
    MSP_TIER_CHANGED,
    CLIENT_CREATED,
    CLIENT_UPDATED,
    CLIENT_ASSIGNED,
    CLIENT_REMOVED,
    CLIENT_STATUS_CHANGED,
    PRODUCT_ASSIGNED,
    PRODUCT_REMOVED,
    PRODUCT_UPDATED,
    CREDIT_ENABLED,
    CREDIT_DISABLED,
    CREDIT_UPDATED
}

