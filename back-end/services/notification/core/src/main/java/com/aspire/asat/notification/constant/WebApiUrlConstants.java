package com.aspire.asat.notification.constant;

public class WebApiUrlConstants {



    private WebApiUrlConstants() {
    }

    public static final String API_PREFIX = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_URI_ROOT = API_PREFIX + API_VERSION;


    //notification
    public static final String NOTIFICATION_API = API_URI_ROOT;
    public static final String NOTIFICATION_SENDING_PATH = "/send";
    public static final String NOTIFICATION_HEALTH_CHECK = "/health-check";

    public static final String NOTIFICATION_MANAGEMENT_API = API_URI_ROOT + "/management" ;

    public static final String ADMIN_NOTIFICATION_SETTINGS_DYNAMIC_API = API_URI_ROOT + "/admin/notification-settings/dynamic";

    public static final String ADMIN_NOTIFICATION_ROLE_SETTINGS_API = API_URI_ROOT + "/admin/notification-settings/roles";

    public static final String CLIENT_NOTIFICATION_SETTINGS_API = API_URI_ROOT + "/client/notification-settings";

    public static final String USER_NOTIFICATION_SETTINGS_API = API_URI_ROOT + "/user/notification-settings";


    //test endpoints
    public static final String TEST_SMTP_SEND = "/test/smtp/send";
    public static final String TEST_SMTP_SIMPLE = "/test/smtp/simple";
    public static final String TEST_SMS_SEND = "/test/sms/send";
    public static final String TEST_SMS_SIMPLE = "/test/sms/simple";

}
