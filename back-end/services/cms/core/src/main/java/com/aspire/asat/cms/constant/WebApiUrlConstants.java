package com.aspire.asat.cms.constant;

public class WebApiUrlConstants {

    private WebApiUrlConstants() {
        // private constructor to prevent instantiation
    }

    public static final String API_PREFIX = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_URI_ROOT = API_PREFIX + API_VERSION;

    public static final String PATH_VAR_ID = "/{id}";
    public static final String PATH_VAR_SEARCH = "/search";
    public static final String PATH_VAR_FILTER = "/filter";
    public static final String PATH_VAR_EXPORT = "/export";
    public static final String PATH_VAR_BULK_EXPORT = "/bulk-export";
    public static final String PATH_VAR_CONTENT_TEXT = "/text";
    public static final String PATH_VAR_CONTENT_LINK = "/link";
    public static final String PATH_VAR_CONTENT_SLIDE = "/slide";
    public static final String PATH_VAR_CONTENT_PDF = "/pdf";
    public static final String PATH_VAR_CONTENT_STORY_BLOCK = "/story-block";
    public static final String PATH_VAR_BULK_DELETE = "/bulk-delete";
    public static final String PATH_VAR_BULK_UPDATE = "/bulk-update";
    public static final String PATH_VAR_ENABLED = "/enabled";
    public static final String EXISTS = "/exists";
    public static final String CLIENT_PRODUCTS = "/client/products";
    public static final String CLIENT_ASSIGNED_PACKAGES = "/client/assigned-packages";
    public static final String PATH_VAR_ASSIGN = "/assigned";

    // Products
    public static final String PRODUCT_API = API_URI_ROOT + "/products";

    // Courses
    public static final String COURSE_API = API_URI_ROOT + "/courses";

    // Chapter
    public static final String CHAPTER_API = API_URI_ROOT + "/chapters";

    // Package
    public static final String PACKAGE_API = API_URI_ROOT + "/packages";

    // Base Package Config
    public static final String BASE_PACKAGE_CONFIG_API = API_URI_ROOT + "/base-package-configs";

    // Feature
    public static final String FEATURE_API = API_URI_ROOT + "/features";

    // Content
    public static final String CONTENT_API = API_URI_ROOT + "/contents";

    // Question
    public static final String QUESTION_API = API_URI_ROOT + "/questions";

    // Storage
    public static final String STORAGE_API = API_URI_ROOT + "/storages";

    // Client
    public static final String CLIENT_API = API_URI_ROOT + "/client";
    public static final String PACKAGE_ASSIGNMENT_REPORT_API = CLIENT_API + "/package-assignment-report";
    public static final String LICENSE_ASSIGNMENTS_API = CLIENT_API + "/license-assignments";
    public static final String TOPIC_ASSIGNMENT_REPORT_API = CLIENT_API + "/topic-assignment-report";
    public static final String PHISHING_COURSE_PATH = CLIENT_API + "/phishing-course";

    // Package-exam
    public static final String PACKAGE_EXAM_API = API_URI_ROOT + "/package-exams";

    //Topic
    public static final String TOPIC_API = API_URI_ROOT + "/topics";

    //Country
    public static final String COUNTRY_API = API_URI_ROOT + "/countries";

    //category
    public static final String CATEGORY_API = API_URI_ROOT + "/categories";

    //tags
    public static final String TAG_API = API_URI_ROOT + "/tags";

    //compliance
    public static final String COMPLIANCE_API = API_URI_ROOT + "/compliances";

    //content nt typ
    public static final String CONTENT_TYPE_API = API_URI_ROOT + "/content-types";

    public static final String SUB_PACKAGE_API = API_URI_ROOT + "/sub-packages";

    // Client Exam Settings
    public static final String CLIENT_EXAM_SETTINGS_API = API_URI_ROOT + "/exam-settings";

    public static final String SUB_PACKAGE_TOPIC_API = API_URI_ROOT + "/sub-package-topics";

    // User Licence
    public static final String USER_LICENCE_API = API_URI_ROOT + "/user-licences";

    // Client Dashboard
    public static final String CLIENT_DASHBOARD_API = API_URI_ROOT + "/client-dashboard";

    // Client Admin Product Usage Report
    public static final String CLIENT_ADMIN_PRODUCT_USAGE_REPORT_API = API_URI_ROOT + "/client-admin/product-usage-report";

    // Training completed + certificate earned counts (used by registration onboarding report)
    public static final String TRAINING_CERTIFICATE_COUNT_API = API_URI_ROOT + "/reports/training-certificate-counts";

    // Client Product
    public static final String CLIENT_PRODUCT_API = API_URI_ROOT + "/client-product";

    // Organization Dashboard
    public static final String ORGANIZATION_DASHBOARD_API = API_URI_ROOT + "/organization-dashboard";

    // User Range
    public static final String USER_RANGE_API = API_URI_ROOT + "/user-ranges";

    // Package Range Pricing
    public static final String PACKAGE_RANGE_PRICING_API = API_URI_ROOT + "/package-range-pricing";

    // Certificate Template
    public static final String CERTIFICATE_TEMPLATE_API = API_URI_ROOT + "/certificate-templates";

    // Client Certificate Template
    public static final String CLIENT_CERTIFICATE_TEMPLATE_API = API_URI_ROOT + "/client-certificate-templates";

    // Contents Available Language
    public static final String CONTENTS_AVAILABLE_LANGUAGE_API = API_URI_ROOT + "/contents-available-languages";

}
