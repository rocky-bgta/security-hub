package com.aspire.asat.breachdetection.constant;

public class WebApiUrlConstants {
    public static final String BREACH_DETECTION_API = "/api/v1";

    private WebApiUrlConstants() {
    }

    public static final String INSECURE_WEB_API = BREACH_DETECTION_API + "/insecure-web";
    public static final String SHODAN_API = BREACH_DETECTION_API + "/shodan";
}
