package com.aspire.asat.registration.constant;

public class WebApiUrlConstants {

    public final static String API_PREFIX = "/api";
    public final static String API_VERSION = "/v1";
    public final static String API_URI_ROOT = API_PREFIX + API_VERSION;
    public final static String PATH_VAR_ID = "/{id}";
    public final static String PATH_VAR_SEARCH = "/search";
    public final static String PATH_VAR_UPLOAD = "/upload-csv";
    public final static String PATH_VAR_USER_INFO = "/user-info";
    public final static String NO_PROFILE = "https://asatstorageeastus.blob.core.windows.net/c-asatv2-registration/NoProfilePic.jpg";

    // Client
    public final static String CLIENT_API = API_URI_ROOT + "/client";
    public final static String BULK_CLIENT_API = "/bulk-client";
    // Msp
    public final static String MSP_API = API_URI_ROOT + "/msp";
    public final static String MSP_PRODUCT_API = API_URI_ROOT + "/msp-product";
    public final static String MSP_DASHBOARD = API_URI_ROOT + "/msp-dashboard";
    public final static String MSP_CLIENT_LICENSES = "/client-licenses";

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
    public static final String BRANDING = API_URI_ROOT + "/branding";
    public static final String DASHBOARD = API_URI_ROOT + "/dashboard";

    // META DATA API
    public static final String METADATA = API_URI_ROOT + "/metadata";
    public static final String ORGANIZATION_TYPE = METADATA + "/organization-type";

    // USER ACTIVITY REPORTS API
    public static final String USER_ACTIVITY_API = API_URI_ROOT + "/reports/user-activity";

    // USER SUMMARY REPORT API
    public static final String USER_SUMMARY_REPORT_API = API_URI_ROOT + "/reports/user-summary";

    // SUBSCRIPTION SUMMARY REPORT API
    public static final String SUBSCRIPTION_SUMMARY_REPORT_API = API_URI_ROOT + "/reports/subscription-summary";

    // PRODUCT LICENSE ONBOARDING REPORT API
    public static final String PRODUCT_LICENSE_ONBOARDING_REPORT_API = API_URI_ROOT + "/reports/product-license-onboarding";

    // REQUIRED INFO API
    public static final String REQUIRED_INFO_API = API_URI_ROOT + "/required-info";

    // ACTIVITY LOG API
    public static final String ACTIVITY_LOG_API = API_URI_ROOT + "/activity-log";

    // SUPER ADMIN API
    public static final String SUPER_ADMIN_API = API_URI_ROOT + "/super-admin";
}
