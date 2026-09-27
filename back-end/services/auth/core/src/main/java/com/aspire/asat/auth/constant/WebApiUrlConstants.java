package com.aspire.asat.auth.constant;

public class WebApiUrlConstants {
    public static final String API_PREFIX = "/api";
    public static final String API_VERSION = "/v1";
    public static final String AUTH_BASE_URL = "/auth";
    public static final String API_BASE_URL = API_PREFIX + API_VERSION + AUTH_BASE_URL;
    public static final String AUTH_LOGIN_URL = API_BASE_URL + "/login";
    public static final String AUTH_LOGOUT_URL = API_BASE_URL + "/logout";


    public static final String AUTH_HOME_INDEX = API_BASE_URL + "/";



}
