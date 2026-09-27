package com.aspire.asat.billing.constant;

public class WebApiUrlConstants {

    public final static String API_PREFIX = "/api";
    public final static String API_VERSION = "/v1";
    public final static String API_URI_ROOT = API_PREFIX + API_VERSION;
    // Client
    public final static String CLIENT_API = API_URI_ROOT + "/client";
    // Msp
    public final static String MSP_API = API_URI_ROOT + "/msp";
    // Content
    public final static String CONTENT_API = API_URI_ROOT + "/content";
    // License
    public final static String LICENSE_API = API_URI_ROOT + "/license";
    // Lesson
    public final static String LESSON_API = API_URI_ROOT + "/lesson";
    // Course
    public final static String COURSE_API = API_URI_ROOT + "/course";
    // Packages
    public final static String PACKAGES_API = API_URI_ROOT + "/packages";
    // Stats
    public final static String STATS_API = API_URI_ROOT + "/stats";
    // Info
    public final static String INFO_API = API_URI_ROOT + "/info";
    // User
    public final static String USER_API = API_URI_ROOT + "/user";
    // Billing
    public final static String PAYMENT_API = API_URI_ROOT + "/payment";
    // Manual Payment
    public final static String MANUAL_PAYMENT_API = API_URI_ROOT + "/manual-payment";
    // Form
    public final static String USER_FORM_API = API_URI_ROOT + "/form-data";
    // Translation
    public final static String TRANSLATION_API = API_URI_ROOT + "/translate";
    public final static String PATH_VAR_ID = "/{id}";
    public final static String PATH_VAR_SEARCH = "/search";
    public final static String PATH_VAR_UPLOAD = "/upload-csv";
    public final static String PATH_VAR_USER_INFO = "/user-info";
    public final static String NO_PROFILE = "https://asatstorageeastus.blob.core.windows.net/c-asatv2-registration/NoProfilePic.jpg";
    public final static String CONTENT_COUNT = "/count";
    public final static String LICENSE_COUNT = "/count";
    // logo
    public final static String CLIENT_LOGO = "AspireTech_logo_client_";
    public final static String USER_LOGO = "AspireTech_logo_user_";
    public final static String MSP_LOGO = "AspireTech_logo_msp_";
    public final static String BULK_USER_API = "/bulk-user";

//    gateways
    public final static String STRIPE_API = "/stripe";
    public final static String PAYPAL = "/paypal";
    public final static String WEBHOOK = "/webhook";
    public final static String PAYMENT = "/payment";

//    manual-billing
    public final static String CLIENT_INFO = "/client-info";
    public final static String PAYMENT_ENTRY = "/manual-entry";
    public final static String ONLINE_PAYMENT_ENTRY = "/online-entry";

//    coupon
    public final static String COUPON_API = API_URI_ROOT + "/coupon";

//    commision
    public final static String COMMISSION_API = API_URI_ROOT + "/commission";

//    credit
    public final static String CREDIT_API = API_URI_ROOT + "/credit";

    public static final String INVOICE_API =  API_URI_ROOT + "/invoice";

    // Basic Crud
    public final static String CREATE = "/create";
    public final static String UPDATE = "/update";
    public final static String DELETE = "/delete";
    public final static String GET = "/get";
    public final static String GET_ALL = "/get-all";
    public final static String GET_BY_ID = "/get-by-id";
    public final static String GET_BY_IDS = "/get-by-ids";
    public final static String GET_BY_QUERY = "/get-by-query";
    public final static String GET_BY_FIELD = "/get-by-field";
    public final static String GET_BY_FIELDS = "/get-by-fields";
    // Batch Size
    public final static int BATCH_SIZE = 1000;


    public static final String VAT_API = API_URI_ROOT + "/vat";

    // Analytics
    public static final String ANALYTICS_API = API_URI_ROOT + "/analytics";

    // Refund
    public static final String REFUND_API = API_URI_ROOT + "/refund";
}
