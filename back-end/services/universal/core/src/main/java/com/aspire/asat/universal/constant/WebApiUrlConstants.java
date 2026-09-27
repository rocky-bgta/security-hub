package com.aspire.asat.universal.constant;

public class WebApiUrlConstants {

    public final static String API_PREFIX = "/api";
    public final static String API_VERSION = "/v1";
    public final static String API_URI_ROOT = API_PREFIX + API_VERSION;
    // Client
    public final static String CLIENT_API = API_URI_ROOT + "/client";
    public final static String BULK_CLIENT_API = "/bulk-client";
    // Msp
    public final static String MSP_API = API_URI_ROOT + "/msp";

    // Content
    public final static String CONTENT_API = API_URI_ROOT + "/content";
    public final static String CONTENT_COUNT = "/count";

    // License
    public final static String LICENSE_API = API_URI_ROOT + "/license";
    public final static String LICENSE_COUNT = "/count";

    // Lesson
    public final static String LESSON_API = API_URI_ROOT + "/lesson";

    // Course
    public final static String COURSE_API = API_URI_ROOT + "/course";

    // Notification
    public final static String NOTIFICATION_API = API_URI_ROOT + "/notification";

    // Packages
    public final static String PACKAGES_API = API_URI_ROOT + "/packages";

    // Stats
    public final static String STATS_API = API_URI_ROOT + "/stats";

    // logo
    public final static String CLIENT_LOGO = "AspireTech_logo_client_";
    public final static String USER_LOGO = "AspireTech_logo_user_";
    public final static String MSP_LOGO = "AspireTech_logo_msp_";

    // Info
    public final static String INFO_API = API_URI_ROOT + "/info";

    // User
    public final static String SYSTEM_USER_API = API_URI_ROOT + "/system/user";
    public final static String BULK_USER_API = "/bulk-user";

    // Form
    public final static String USER_FORM_API = API_URI_ROOT + "/form-data";

    // Translation
    public final static String TRANSLATION_API = API_URI_ROOT + "/translate";

    // Company
    public final static String COMPANY_NAME = "/company/{companyName}";

    // Batch Size
    public final static int BATCH_SIZE = 1000;

    // Batch Size
    public final static int DAYS = 3;

    public static final String CLIENT_ADMIN_API = API_URI_ROOT + "/client/admin";

    public static final String ROLE_API = API_URI_ROOT + "/role";
    public static final String ROLE_PERMISSION_API = API_URI_ROOT + "/role-permissions";
    public static final String END_USER = API_URI_ROOT + "/end-user";
    public static final String DEPARTMENTS = API_URI_ROOT + "/departments";

    // META DATA API
    public static final String METADATA = API_URI_ROOT + "/metadata";
    public static final String ORGANIZATION_TYPE = METADATA + "/organization-type";

    // USER ACTIVITY REPORTS API
    public static final String USER_ACTIVITY_API = API_URI_ROOT + "/reports/user-activity";
}
