package com.aspire.asat.notification.utils;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.config.NotificationDataProperties;
import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.repository.NotificationSettingsRepository;
import com.aspire.asat.notification.repository.NotificationTemplateRepository;
import com.aspire.asat.notification.service.NotificationRoleSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data initializer service that runs on application startup
 * to insert test data for a notification system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDataInitializer implements CommandLineRunner {

    private final NotificationSettingsRepository settingsRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationDataProperties dataProperties;
    private final NotificationRoleSettingsService roleSettingsService;

    /**
     * Notification types that must have SMS enabled globally to match the MSP role scope
     * matrix (7 matrix rows plus the {@code USER_SUSPENSION} and {@code SUBPACKAGE_EXPIRY_REMINDER}
     * aliases, which must carry identical flags to the canonical types the matrix names).
     */
    private static final Set<NotificationType> SMS_ENABLED_GLOBAL_TYPES = Set.of(
            NotificationType.USER_SUSPENDED,
            NotificationType.USER_SUSPENSION,
            NotificationType.PACKAGE_EXPIRY,
            NotificationType.SUBPACKAGE_EXPIRY_REMINDER,
            NotificationType.PAYMENT_FAILURE,
            NotificationType.PENDING_PAYMENT,
            NotificationType.SUBSCRIPTION_RENEWAL_REMINDER,
            NotificationType.SYSTEM_HEALTH_ALERTS,
            NotificationType.SECURITY_ALERTS
    );

    /**
     * Notification types the supplied MSP scope matrix has no row for (user/manager/HR/C-level
     * scoped events); MSP is disabled for these types.
     */
    private static final Set<NotificationType> MSP_DISABLED_TYPES = Set.of(
            NotificationType.WELCOME_EMAIL_CLIENT_USER,
            NotificationType.WELCOME_CLIENT_EMAIL,
            NotificationType.WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD,
            NotificationType.PASSWORD_RESET_BY_ASPIRE,
            NotificationType.USER_ACTIVATED,
            NotificationType.COURSE_COMPLETION_USER,
            NotificationType.COURSE_NOT_STARTED_USER,
            NotificationType.COURSE_NOT_STARTED_MANAGER,
            NotificationType.COURSE_NOT_STARTED_C_LEVEL,
            NotificationType.COURSE_EXPIRY_HR,
            NotificationType.MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL,
            NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL,
            NotificationType.DOMAIN_VERIFICATION
    );

    /** The only matrix row where MSP email is off. */
    private static final Set<NotificationType> MSP_EMAIL_OFF_TYPES = Set.of(NotificationType.LEADERBOARD_UPDATE);

    /** The only matrix row where MSP in-app is off. */
    private static final Set<NotificationType> MSP_IN_APP_OFF_TYPES = Set.of(NotificationType.WELCOME_EMAIL);

    /** Third-person / alert SMS copy for admin role variants (SMS-matrix types). */
    private static final String ADMIN_SMS_USER_SUSPENDED =
            "ASAT alert: user {{userName}} account suspended: {{reason}}.";
    private static final String ADMIN_SMS_PACKAGE_EXPIRY =
            "ASAT alert: package '{{packageName}}' for {{userName}} expires on {{expirationDate}}.";
    private static final String ADMIN_SMS_PAYMENT_FAILURE =
            "ASAT alert: payment of {{amount}} failed: {{failureReason}}.";
    private static final String ADMIN_SMS_PENDING_PAYMENT =
            "ASAT alert: payment of {{amount}} pending, due {{dueDate}}.";
    private static final String ADMIN_SMS_SUBSCRIPTION_RENEWAL =
            "ASAT alert: subscription '{{subscriptionName}}' expires on {{renewalDate}}.";
    private static final String ADMIN_SMS_SYSTEM_HEALTH =
            "ASAT system alert: {{reason}}. Our team is working to resolve this.";
    private static final String ADMIN_SMS_SECURITY_ALERTS =
            "ASAT security alert for {{userName}}: verification code issued. Expires in {{expiryMinutes}} min.";

    // ── Aspire Design System ─────────────────────────────────────────────
    // Palette derived from index.html:
    //   Outer bg: #e8edf2   Hero bg: #fafafa   Body bg: #ffffff
    //   Accent: #42b9ea    Text dark: #1a2742   Text body: #444444
    //   Card bg: #f0f8fd   Card border: #c8e4f5 / #dceef7
    //   Info: #42b9ea / #f0f8fd   Success: #27ae60 / #e8f5e9
    //   Warning: #f39c12 / #fff8e1  Danger: #e74c3c / #fce4ec
    // ─────────────────────────────────────────────────────────────────────

    private static final String DEFAULT_SIGNOFF = "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">The {{companyName}} Team</span>";
    private static final String ASPIRE_TEAM_SIGNOFF = "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">The Aspire Tech Team</span>";
    private static final String ASPIRE_ADMIN_SIGNOFF = "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">AspireTech Admin</span>";

    // ── Email Shell Builder ──────────────────────────────────────────────

    private static String buildEmail(String title, String heading, String bodyRows) {
        return buildEmail(title, heading, bodyRows, DEFAULT_SIGNOFF);
    }

    private static String buildEmail(String title, String heading, String bodyRows, String signoff) {
        return "<!doctype html><html lang=\"en\">" +
                "<head><meta charset=\"utf-8\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" /><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" /><meta name=\"x-apple-disable-message-reformatting\" />" +
                "<title>" + title + "</title>" +
                "<style type=\"text/css\">body,p,h1,h2,h3,h4,h5,h6,td,div{margin:0;padding:0}img{border:0;line-height:100%;outline:none;text-decoration:none;-ms-interpolation-mode:bicubic;display:block}table{border-collapse:collapse !important;mso-table-lspace:0pt;mso-table-rspace:0pt}table td{border-collapse:collapse}body{height:100%!important;margin:0!important;padding:0!important;width:100%!important;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;background-color:#e8edf2}@media screen and (max-width:620px){.email-container{width:100%!important;max-width:100%!important}.mobile-padding{padding-left:20px!important;padding-right:20px!important}}</style>" +
                "</head>" +
                "<body width=\"100%\" style=\"margin:0;padding:0!important;mso-line-height-rule:exactly;background-color:#e8edf2\">" +
                "<div style=\"display:none;max-height:0;overflow:hidden;mso-hide:all\">" + title + "&nbsp;&zwnj;&nbsp;&zwnj;&nbsp;&zwnj;&nbsp;&zwnj;&nbsp;&zwnj;&nbsp;&zwnj;&nbsp;&zwnj;&nbsp;&zwnj;</div>" +
                "<div style=\"width:100%;background-color:#e8edf2\">" +
                "<table align=\"center\" role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"600\" style=\"margin:auto;max-width:600px\" class=\"email-container\">" +
                // Top spacer
                "<tr><td bgcolor=\"#e8edf2\" style=\"height:24px;font-size:1px;line-height:1px\">&nbsp;</td></tr>" +
                // Hero section
                "<tr><td bgcolor=\"#fafafa\" style=\"padding:0\">" +
                "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"600\" style=\"width:600px\"><tr><td valign=\"middle\" style=\"padding:36px 12px;text-align:center\">" +
                "<img src=\"{{logoUrl}}\" alt=\"{{companyName}} Logo\" width=\"56\" style=\"display:inline-block;width:56px;height:auto;margin:0 auto 12px auto\" />" +
                "<p style=\"margin:0 0 4px 0;font-family:Arial,Helvetica,sans-serif;font-size:18px;font-weight:700;color:#000000;mso-color-alt:#000000;letter-spacing:2px;text-transform:uppercase;text-align:center\">ASPIRE TECH</p>" +
                "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" style=\"margin:10px auto 14px auto\"><tr><td bgcolor=\"#000000\" style=\"width:48px;height:2px;font-size:1px;line-height:1px\">&nbsp;</td></tr></table>" +
                "<h1 style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:22px;font-weight:700;color:#000000;mso-color-alt:#000000;letter-spacing:0.5px;line-height:1.3;text-align:center\">" + heading + "</h1>" +
                "</td></tr></table></td></tr>" +
                // Accent border
                "<tr><td bgcolor=\"#42b9ea\" style=\"height:3px;font-size:1px;line-height:1px\">&nbsp;</td></tr>" +
                // Body content
                bodyRows +
                // Sign-off
                "<tr><td bgcolor=\"#ffffff\" style=\"padding:0 28px 30px 28px;border-bottom:3px solid #42b9ea\" class=\"mobile-padding\">" +
                "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:1.7;color:#444444\">" + signoff + "</p>" +
                "</td></tr>" +
                // Footer
                "<tr><td bgcolor=\"#fafafa\" style=\"padding:22px 24px\">" +
                "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"><tr><td style=\"padding-bottom:14px;text-align:center;font-family:Arial,Helvetica,sans-serif;font-size:12px;line-height:1.8;color:#000000;mso-color-alt:#000000\">" +
                "<a href=\"tel:+19176009233\" style=\"color:#000000;mso-color-alt:#000000;text-decoration:none\">+1.917.600.9233</a>&nbsp;&#124;&nbsp;" +
                "<a href=\"mailto:info@aspiretss.com\" style=\"color:#000000;mso-color-alt:#000000;text-decoration:none\">info@aspiretss.com</a><br />" +
                "<span style=\"color:#000000;mso-color-alt:#000000\">11 Broadway, New York, NY 10004, USA</span>" +
                "</td></tr></table>" +
                // Social icons
                "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" style=\"margin:0 auto 16px auto\"><tr>" +
                "<td style=\"padding:0 8px\"><a href=\"https://www.facebook.com/AspireSAT\" style=\"text-decoration:none\"><img src=\"https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/email-template/facebook.png\" alt=\"Facebook\" width=\"22\" height=\"22\" style=\"display:block;width:22px;height:22px\" /></a></td>" +
                "<td style=\"padding:0 8px\"><a href=\"https://x.com/AspireTechServ3\" style=\"text-decoration:none\"><img src=\"https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/email-template/x.png\" alt=\"X\" width=\"22\" height=\"22\" style=\"display:block;width:22px;height:22px\" /></a></td>" +
                "<td style=\"padding:0 8px\"><a href=\"https://www.youtube.com/@aspiretechservicesandsolut578\" style=\"text-decoration:none\"><img src=\"https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/email-template/youtube.png\" alt=\"YouTube\" width=\"22\" height=\"22\" style=\"display:block;width:22px;height:22px\" /></a></td>" +
                "<td style=\"padding:0 8px\"><a href=\"https://www.linkedin.com/company/aspire-tech-security-awareness-training\" style=\"text-decoration:none\"><img src=\"https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/email-template/linkedin.png\" alt=\"LinkedIn\" width=\"22\" height=\"22\" style=\"display:block;width:22px;height:22px\" /></a></td>" +
                "<td style=\"padding:0 8px\"><a href=\"https://www.instagram.com/aspire_sat\" style=\"text-decoration:none\"><img src=\"https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/email-template/instagram.png\" alt=\"Instagram\" width=\"22\" height=\"22\" style=\"display:block;width:22px;height:22px\" /></a></td>" +
                "</tr></table>" +
                // Legal
                "<p style=\"margin:0 0 8px 0;font-family:Arial,Helvetica,sans-serif;font-size:11px;line-height:1.6;color:#000000;mso-color-alt:#000000;text-align:center\">This is an automated system notification. Please do not reply directly.<br />For security policy inquiries or to contact the SOC, use the links below.</p>" +
                "<p style=\"margin:0 0 8px 0;font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#000000;mso-color-alt:#000000;text-align:center\">&copy; {{currentYear}} {{companyName}}. All Rights Reserved.</p>" +
                "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:11px;text-align:center\">" +
                "<a href=\"https://securityawarenesstraining.ai/privacy-policy\" style=\"color:#42b9ea;mso-color-alt:#42b9ea;text-decoration:underline\">Security Policy</a>&nbsp;&#124;&nbsp;" +
                "<a href=\"https://securityawarenesstraining.ai/terms-service\" style=\"color:#42b9ea;mso-color-alt:#42b9ea;text-decoration:underline\">Terms of Service</a>&nbsp;&#124;&nbsp;" +
                "<a href=\"https://securityawarenesstraining.ai/company/contact\" style=\"color:#42b9ea;mso-color-alt:#42b9ea;text-decoration:underline\">Contact Us</a></p>" +
                "</td></tr>" +
                // Bottom spacer
                "<tr><td bgcolor=\"#e8edf2\" style=\"height:24px;font-size:1px;line-height:1px\">&nbsp;</td></tr>" +
                "</table></div></body></html>";
    }

    // ── Content Helpers ──────────────────────────────────────────────────

    private static String bodyTd(String content) {
        return "<tr><td bgcolor=\"#ffffff\" style=\"padding:32px 28px 24px 28px\" class=\"mobile-padding\">" + content + "</td></tr>";
    }

    private static String extraTd(String content) {
        return "<tr><td bgcolor=\"#ffffff\" style=\"padding:0 28px 24px 28px\" class=\"mobile-padding\">" + content + "</td></tr>";
    }

    private static String p(String text) {
        return "<p style=\"margin:0 0 15px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:1.7;color:#444444;mso-color-alt:#444444\">" + text + "</p>";
    }

    private static String pLast(String text) {
        return "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:1.7;color:#444444;mso-color-alt:#444444\">" + text + "</p>";
    }

    private static String detail(String label, String value) {
        return "<p style=\"margin:0 0 3px 0;padding-top:14px;font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#777777;mso-color-alt:#777777;text-transform:uppercase;letter-spacing:1px\">" + label + "</p>" +
                "<p style=\"margin:0;padding-bottom:10px;border-bottom:1px solid #dceef7;font-family:Arial,Helvetica,sans-serif;font-size:15px;font-weight:700;color:#1a2742;mso-color-alt:#1a2742\">" + value + "</p>";
    }

    private static String detailLast(String label, String value) {
        return "<p style=\"margin:0 0 3px 0;padding-top:14px;font-family:Arial,Helvetica,sans-serif;font-size:11px;color:#777777;mso-color-alt:#777777;text-transform:uppercase;letter-spacing:1px\">" + label + "</p>" +
                "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:15px;font-weight:700;color:#1a2742;mso-color-alt:#1a2742\">" + value + "</p>";
    }

    private static String link(String url, String text) {
        return "<a href=\"" + url + "\" style=\"color:#1a6fa0;mso-color-alt:#1a6fa0;text-decoration:none\">" + text + "</a>";
    }

    private static String mailto(String email) {
        return "<a href=\"mailto:" + email + "\" style=\"color:#1a6fa0;mso-color-alt:#1a6fa0;text-decoration:none\">" + email + "</a>";
    }

    private static String infoBox(String heading, String content) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\" bgcolor=\"#42b9ea\" style=\"border-radius:6px;margin-bottom:20px\">" +
                "<tr><td bgcolor=\"#f0f8fd\" style=\"padding:20px;border-radius:4px\">" +
                "<p style=\"margin:0 0 12px 0;padding-bottom:12px;border-bottom:1px solid #c8e4f5;font-family:Arial,Helvetica,sans-serif;font-size:13px;font-weight:700;color:#0d1526;mso-color-alt:#0d1526;letter-spacing:2px;text-transform:uppercase\">" + heading + "</p>" +
                content + "</td></tr></table>";
    }

    private static String successBox(String heading, String content) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\" bgcolor=\"#27ae60\" style=\"border-radius:6px;margin-bottom:20px\">" +
                "<tr><td bgcolor=\"#e8f5e9\" style=\"padding:20px;border-radius:4px\">" +
                "<p style=\"margin:0 0 12px 0;padding-bottom:12px;border-bottom:1px solid #b8dfbe;font-family:Arial,Helvetica,sans-serif;font-size:13px;font-weight:700;color:#27ae60;mso-color-alt:#27ae60;letter-spacing:2px;text-transform:uppercase\">" + heading + "</p>" +
                content + "</td></tr></table>";
    }

    private static String warningBox(String heading, String content) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\" bgcolor=\"#f39c12\" style=\"border-radius:6px;margin-bottom:20px\">" +
                "<tr><td bgcolor=\"#fff8e1\" style=\"padding:20px;border-radius:4px\">" +
                "<p style=\"margin:0 0 12px 0;padding-bottom:12px;border-bottom:1px solid #f5d98e;font-family:Arial,Helvetica,sans-serif;font-size:13px;font-weight:700;color:#f39c12;mso-color-alt:#f39c12;letter-spacing:2px;text-transform:uppercase\">" + heading + "</p>" +
                content + "</td></tr></table>";
    }

    private static String dangerBox(String heading, String content) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\" bgcolor=\"#e74c3c\" style=\"border-radius:6px;margin-bottom:20px\">" +
                "<tr><td bgcolor=\"#fce4ec\" style=\"padding:20px;border-radius:4px\">" +
                "<p style=\"margin:0 0 12px 0;padding-bottom:12px;border-bottom:1px solid #f4b0b0;font-family:Arial,Helvetica,sans-serif;font-size:13px;font-weight:700;color:#e74c3c;mso-color-alt:#e74c3c;letter-spacing:2px;text-transform:uppercase\">" + heading + "</p>" +
                content + "</td></tr></table>";
    }

    private static String neutralBox(String content) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\" bgcolor=\"#cccccc\" style=\"border-radius:6px;margin-bottom:20px\">" +
                "<tr><td bgcolor=\"#f8f9fa\" style=\"padding:20px;border-radius:4px\">" + content + "</td></tr></table>";
    }

    private static String codeBox(String content) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\" bgcolor=\"#42b9ea\" style=\"border-radius:8px;margin-bottom:20px\">" +
                "<tr><td bgcolor=\"#f8f9fa\" style=\"padding:30px;text-align:center;border-radius:6px\">" + content + "</td></tr></table>";
    }

    private static String ctaRow(String url, String text) {
        return "<tr><td bgcolor=\"#ffffff\" style=\"padding:0 28px 8px 28px;text-align:center\" class=\"mobile-padding\">" +
                "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" style=\"margin:0 auto\"><tr><td align=\"center\">" +
                "<a href=\"" + url + "\" style=\"color:#000000;display:inline-block;font-family:Arial,Helvetica,sans-serif;font-size:14px;font-weight:700;line-height:44px;text-align:center;text-decoration:underline;width:200px;-webkit-text-size-adjust:none\">" + text + "</a>" +
                "</td></tr></table></td></tr>";
    }

    private static String ul(String... items) {
        StringBuilder sb = new StringBuilder("<ul style=\"margin:0;padding-left:20px;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444;line-height:1.7\">");
        for (String item : items) sb.append("<li>").append(item).append("</li>");
        return sb.append("</ul>").toString();
    }

    private static String ol(String... items) {
        StringBuilder sb = new StringBuilder("<ol style=\"margin:0;padding-left:20px;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444;line-height:1.7\">");
        for (String item : items) sb.append("<li>").append(item).append("</li>");
        return sb.append("</ol>").toString();
    }

    // ── Email Template Constants ─────────────────────────────────────────

    private static final String NEW_USER_REGISTERED_EMAIL_HTML_TEMPLATE = buildEmail(
            "New User Registration - {{companyName}}", "New User Registration",
            bodyTd(
                    "<p style=\"margin:0 0 12px 0;font-family:Arial,Helvetica,sans-serif;font-size:15px;line-height:1.6;color:#1a2742;mso-color-alt:#1a2742;font-weight:600\">Dear {{adminName}},</p>" +
                            p("This automated message confirms that a new user has successfully registered with your system. Please review the registration details below.") +
                            infoBox("User Details",
                                    detail("Full Name", "{{userName}}") +
                                            detail("Email Address", "<a href=\"mailto:{{email}}\" style=\"font-family:Arial,Helvetica,sans-serif;font-size:15px;font-weight:700;color:#1a6fa0;mso-color-alt:#1a6fa0;text-decoration:none\">{{email}}</a>") +
                                            detailLast("Registration Timestamp", "{{registrationDate}}")
                            )
            ) + ctaRow("https://securityawarenesstraining.ai/auth/login", "View in Portal")
    );

    private static final String WELCOME_EMAIL_HTML_TEMPLATE = buildEmail(
            "Welcome to {{companyName}}", "Welcome to {{companyName}}!",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Thank you for joining {{companyName}}! We're excited to have you on board and look forward to supporting your learning journey.") +
                            infoBox("Your Account Details",
                                    detail("Email", "{{email}}") +
                                            detail("Password", "{{password}}") +
                                            detailLast("Login URL", link("{{loginUrl}}", "{{loginUrl}}"))
                            ) +
                            successBox("Platform Features",
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">{{platformFeatures}}</p>"
                            ) +
                            warningBox("Getting Started", ul(
                                    "Complete your profile setup",
                                    "Explore available courses",
                                    "Track your learning progress",
                                    "Earn certificates upon completion"
                            )) +
                            pLast("If you have any questions or need assistance, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
            ) + ctaRow("{{loginUrl}}", "Login to Your Account")
    );

    private static final String WELCOME_EMAIL_WITHOUT_PASSWORD_HTML_TEMPLATE = buildEmail(
            "Welcome to {{companyName}}", "Welcome to {{companyName}}!",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Thank you for joining {{companyName}}! We're excited to have you on board and look forward to supporting your learning journey.") +
                            successBox("Platform Features",
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">{{platformFeatures}}</p>"
                            ) +
                            warningBox("Getting Started", ul(
                                    "Complete your profile setup",
                                    "Explore available courses",
                                    "Track your learning progress",
                                    "Earn certificates upon completion"
                            )) +
                            pLast("If you have any questions or need assistance, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
            )
    );

    private static final String WELCOME_CLIENT_EMAIL_HTML_TEMPLATE = buildEmail(
            "Welcome to Aspire Tech", "Welcome to Aspire Tech!",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Welcome to Aspire Tech! We are thrilled to have you on board and look forward to working together to help your organization succeed with our Security Awareness Training Portal.") +
                            p("Your account has been successfully created! Please find your login details below:") +
                            infoBox("Login Details",
                                    detail("Login URL", link("{{loginUrl}}", "{{loginUrl}}")) +
                                            detail("User ID", "{{email}}") +
                                            detailLast("Temporary Password", "{{password}}")
                            ) +
                            warningBox("&#9888;&#65039; Important: Your Account Security is Our Priority!",
                                    "<p style=\"margin:0 0 15px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">For your protection, you must log in immediately using the credentials above and change your temporary password to a more secure one.</p>" +
                                            "<h4 style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#f39c12\">To change your password:</h4>" +
                                            ol("Log in using the provided temporary password.",
                                                    "Navigate to your account settings and select \"Change Password.\"",
                                                    "Choose a strong, unique password (we recommend using a combination of letters, numbers, and symbols to strengthen your security).")
                            ) +
                            p("Once you've updated your password, you'll have full access to your account and all the features of our Security Awareness Training Portal.") +
                            p("If you encounter any issues or need assistance, our support team is available to help. Feel free to contact us at <a href=\"mailto:{{mspEmail}}\" style=\"color:#1a6fa0;text-decoration:none\">{{mspName}}</a>.") +
                            pLast("Thank you for choosing Aspire Tech. We are committed to ensuring your success and security throughout your journey with us!")
            ), ASPIRE_TEAM_SIGNOFF
    );

    private static final String WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD_HTML_TEMPLATE = buildEmail(
            "Welcome to Aspire Tech", "Welcome to Aspire Tech!",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Welcome to Aspire Tech! We are thrilled to have you on board and look forward to working together to help your organization succeed with our Security Awareness Training Portal.") +
                            p("Your account has been successfully created! Please find your login details below:") +
                            infoBox("Login Details", detailLast("Login URL", link("{{loginUrl}}", "{{loginUrl}}"))) +
                            p("Once you've updated your password, you'll have full access to your account and all the features of our Security Awareness Training Portal.") +
                            p("If you encounter any issues or need assistance, our support team is available to help. Feel free to contact us at <a href=\"mailto:{{mspEmail}}\" style=\"color:#1a6fa0;text-decoration:none\">{{mspName}}</a>.") +
                            pLast("Thank you for choosing Aspire Tech. We are committed to ensuring your success and security throughout your journey with us!")
            ), ASPIRE_TEAM_SIGNOFF
    );

    private static final String USER_PROFILE_UPDATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "User Profile Updated - {{companyName}}", "User Profile Updated",
            bodyTd(p("Dear {{adminName}},") + p("This is to inform you that a user profile has been updated. Please review the details below.") +
                    infoBox("Update Details", detail("User Name", "{{userName}}") + detail("Updated Fields", "{{updatedFields}}") + detailLast("Update Timestamp", "{{timestamp}}")))
    );

    private static final String USER_SUSPENDED_EMAIL_HTML_TEMPLATE = buildEmail(
            "User Account Suspended - {{companyName}}", "User Account Suspended",
            bodyTd(p("Dear {{adminName}},") + p("A user account has been suspended due to a policy violation or inactivity. Please review the details below.") +
                    dangerBox("Suspension Details", detail("User Name", "{{userName}}") + detail("Reason", "{{reason}}") + detailLast("Timestamp", "{{timestamp}}")))
    );

    private static final String USER_SUSPENSION_EMAIL_HTML_TEMPLATE = buildEmail(
            "User Account Suspended - {{userName}}", "User Account Suspended",
            bodyTd(p("Dear {{userName}},") + p("Your account has been suspended due to a policy violation or inactivity. Please review the details below and contact support if you believe this is an error.") +
                    dangerBox("Suspension Details", detail("User Name", "{{userName}}") + detail("Reason", "{{reason}}") + detailLast("Timestamp", "{{timestamp}}"))),
            ASPIRE_ADMIN_SIGNOFF
    );

    private static final String USER_ACTIVATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "User Account Reactivated - {{userName}}", "User Account Reactivated",
            bodyTd(p("Dear {{userName}},") + p("We're pleased to inform you that your account has been successfully reactivated. You now have full access to your account and all platform features.") +
                    successBox("Reactivation Details", detail("User Name", "{{userName}}") + detailLast("Timestamp", "{{timestamp}}"))),
            ASPIRE_ADMIN_SIGNOFF
    );

    private static final String USER_PASSWORD_CHANGE_USER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Password Changed Successfully - {{companyName}}", "&#128274; Password Changed Successfully",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Your password has been successfully changed on {{timestamp}}.") +
                            successBox("&#9989; Change Details", detail("Time", "{{timestamp}}") + detailLast("Status", "Successful")) +
                            warningBox("&#128274; Security Reminder", ul(
                                    "If you didn't make this change, please contact support immediately",
                                    "Keep your password secure and don't share it with anyone"
                            )) +
                            pLast("If you have any questions or concerns, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
            )
    );

    private static final String USER_PASSWORD_CHANGE_ADMIN_EMAIL_HTML_TEMPLATE = buildEmail(
            "User Password Changed - {{companyName}}", "&#128274; User Password Changed",
            bodyTd(
                    p("Dear {{adminName}},") +
                            p("This is to notify you that a user has changed their password.") +
                            infoBox("&#128203; User Details", detail("User Name", "{{userName}}") + detail("Email", "{{userEmail}}") + detailLast("Password Changed At", "{{timestamp}}")) +
                            neutralBox("<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\"><strong>&#8505;&#65039; Information:</strong></p>" +
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">This is an automated notification to keep you informed about account security changes for your users.</p>")
            ), "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">{{companyName}} Team</span>"
    );

    private static final String PASSWORD_RESET_REQUEST_EMAIL_HTML_TEMPLATE = buildEmail(
            "Reset Your Password - {{companyName}}", "&#128274; Reset Your Password",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("We received a request to reset your password for your {{companyName}} account.") +
                            infoBox("&#128274; Security Information", detail("Reset Link Expires In", "{{expiryTime}}") + detailLast("Security Note", "This link will expire after {{expiryTime}} for your security."))
            ) + ctaRow("{{resetUrl}}", "Reset My Password") +
                    extraTd(
                            warningBox("&#9888;&#65039; Important", ul(
                                    "If you didn't request this password reset, please ignore this email",
                                    "Your password will remain unchanged until you create a new one",
                                    "Never share your reset link with anyone",
                                    "For security, change your password immediately after resetting"
                            )) +
                                    p("You can also copy and paste this link into your browser:") +
                                    "<p style=\"margin:0 0 20px 0;background-color:#f0f8fd;padding:10px;border-radius:3px;word-break:break-all;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">{{resetUrl}}</p>" +
                                    pLast("If you have any questions or need assistance, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
                    )
    );

    private static final String PASSWORD_RESET_BY_ASPIRE_EMAIL_HTML_TEMPLATE = buildEmail(
            "Your New Password - {{companyName}}", "&#128274; Your Password Has Been Reset",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Your password for your {{companyName}} account has been reset by an administrator. A new password has been generated for you.") +
                            codeBox(
                                    "<p style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#777777;margin:0 0 15px 0;text-transform:uppercase;letter-spacing:1px\">Your New Password</p>" +
                                            "<p style=\"font-size:24px;font-weight:bold;color:#42b9ea;margin:15px 0;letter-spacing:2px;font-family:'Courier New',Courier,monospace;word-break:break-all\">{{password}}</p>" +
                                            "<p style=\"font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777;margin:15px 0 0 0\">Please use this password to log in and change it immediately</p>"
                            ) +
                            infoBox("&#128274; Security Information", detail("Login URL", link("{{loginUrl}}", "{{loginUrl}}")) + detailLast("Security Note", "Please change it immediately after logging in for your security."))
            ) + ctaRow("{{loginUrl}}", "Login to Your Account") +
                    extraTd(
                            warningBox("&#9888;&#65039; Important Security Reminders", ul(
                                    "Never share your password with anyone",
                                    "Use a strong, unique password that you haven't used elsewhere",
                                    "If you didn't request this password reset, please contact support immediately"
                            )) +
                                    pLast("If you have any questions or need assistance, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
                    )
    );

    private static final String BULK_USER_IMPORT_SUMMARY_EMAIL_HTML_TEMPLATE = buildEmail(
            "Bulk User Import Summary - {{companyName}}", "&#128202; Bulk User Import Summary",
            bodyTd(
                    p("Dear {{adminName}},") +
                            p("Your bulk user import has been completed. Here's a summary of the results:") +
                            infoBox("&#128200; Import Statistics",
                                    "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\"><tr>" +
                                            "<td width=\"50%\" style=\"padding:10px;text-align:center;background-color:#e8f5e9;border-radius:5px\"><h3 style=\"color:#27ae60;margin:0;font-size:24px\">{{totalImported}}</h3><p style=\"color:#27ae60;margin:5px 0 0 0;font-weight:bold;font-family:Arial,Helvetica,sans-serif;font-size:13px\">Successfully Imported</p></td>" +
                                            "<td width=\"10\">&nbsp;</td>" +
                                            "<td width=\"50%\" style=\"padding:10px;text-align:center;background-color:#fce4ec;border-radius:5px\"><h3 style=\"color:#e74c3c;margin:0;font-size:24px\">{{totalSkipped}}</h3><p style=\"color:#e74c3c;margin:5px 0 0 0;font-weight:bold;font-family:Arial,Helvetica,sans-serif;font-size:13px\">Skipped</p></td>" +
                                            "</tr></table>" +
                                            "<p style=\"text-align:center;margin:10px 0 0 0;color:#777777;font-family:Arial,Helvetica,sans-serif;font-size:14px\"><strong>Import Date:</strong> {{importDate}}</p>"
                            ) +
                            successBox("&#9989; Successfully Imported Users ({{totalImported}})",
                                    "<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">The following users have been successfully imported and welcome emails have been sent:</p>" +
                                            "<div style=\"background-color:white;border-radius:5px;padding:15px;margin-top:10px;max-height:400px;overflow-y:auto\"><pre style=\"font-family:'Courier New',monospace;font-size:12px;color:#444444;margin:0;white-space:pre-wrap;word-wrap:break-word\">{{importedUsersList}}</pre></div>"
                            ) +
                            dangerBox("&#9888;&#65039; Skipped Users ({{totalSkipped}})",
                                    "<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">The following users were not imported due to validation issues:</p>" +
                                            "<div style=\"background-color:white;border-radius:5px;padding:15px;margin-top:10px;max-height:400px;overflow-y:auto\"><pre style=\"font-family:'Courier New',monospace;font-size:12px;color:#444444;margin:0;white-space:pre-wrap;word-wrap:break-word\">{{skippedUsersList}}</pre></div>"
                            ) +
                            neutralBox("<h4 style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#1a2742\">&#8505;&#65039; Additional Information</h4>" + ul(
                                    "All successfully imported users have been sent welcome emails with their login credentials",
                                    "Skipped users were not imported due to duplicate emails, invalid country codes, or other validation issues",
                                    "You can retry importing skipped users after addressing the issues mentioned above"
                            )) +
                            pLast("If you have any questions about this import process, please contact our support team.")
            )
    );

    private static final String NEW_COURSE_CREATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "New Course Created - {{companyName}}", "New Course Created",
            bodyTd(p("Dear {{adminName}},") + p("A new training course has been successfully created and is available in the system.") +
                    infoBox("Course Details", detail("Course Title", "{{courseTitle}}") + detail("Course Description", "{{courseDescription}}") + detailLast("Access Permissions", "{{permissions}}")))
    );

    private static final String COURSE_UPDATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "Course Updated - {{companyName}}", "Course Updated",
            bodyTd(p("Dear {{adminName}},") + p("An existing course has been updated in the system.") +
                    infoBox("Update Details", detail("Course Title", "{{courseTitle}}") + detail("Updated Fields", "{{updatedFields}}") + detailLast("Update Timestamp", "{{timestamp}}")))
    );

    private static final String COURSE_ASSIGNED_EMAIL_HTML_TEMPLATE = buildEmail(
            "Course Assigned - {{companyName}}", "Course Assigned",
            bodyTd(p("Dear {{adminName}},") + p("A training course has been assigned to a user. Please review the assignment details below.") +
                    infoBox("Assignment Details", detail("User Name", "{{userName}}") + detail("Course Title", "{{courseTitle}}") + detailLast("Assignment Date", "{{assignmentDate}}")))
    );

    private static final String COURSE_COMPLETION_EMAIL_HTML_TEMPLATE = buildEmail(
            "Course Completed - {{companyName}}", "Course Completed",
            bodyTd(p("Dear {{adminName}},") + p("A user has successfully completed a training course. Details are provided below.") +
                    successBox("Completion Details", detail("User Name", "{{userName}}") + detail("Course Title", "{{courseTitle}}") + detailLast("Completion Timestamp", "{{timestamp}}")))
    );

    private static final String COURSE_COMPLETION_USER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Course Completed – {{courseTitle}}", "Course Completed",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Congratulations! &#127881;<br />You have successfully completed the training course \"<strong>{{courseTitle}}</strong>\".") +
                            successBox("Completion Details",
                                    detail("Course Title", "{{courseTitle}}") + detailLast("Completion Date", "{{completionDate}}")) +
                            pLast("Thank you for completing your assigned training. You can review your progress from your {{companyName}} account.")
            ) + ctaRow("{{loginUrl}}", "View Your Dashboard")
    );

    private static final String NEW_PACKAGE_CREATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "New Package Created - {{companyName}}", "New Package Created",
            bodyTd(p("Dear {{adminName}},") + p("A new service package has been created and is now available. Please review the details below.") +
                    infoBox("Package Details", detail("Package Name", "{{packageName}}") + detail("Package Contents", "{{contents}}") + detailLast("Pricing", "{{pricing}}")))
    );

    private static final String PACKAGE_ASSIGNED_USER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Course Assignment Confirmation - {{packageName}}", "&#128218; Course Assignment Confirmation",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Great news! A new course has been assigned to you.") +
                            successBox("Course Details",
                                    detail("Course Name", "{{packageName}}") +
                                            "<!--<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\"><strong>Course Details::</strong> {{packageDetails}}</p>-->" +
                                            detailLast("Assignment Date", "{{assignmentDate}}")
                            ) +
                            successBox("What's Next?", ul(
                                    "Log in to your {{companyName}} account",
                                    "Access your assigned courses",
                                    "Start your learning journey",
                                    "Track your progress"
                            )) +
                            pLast("If you have any questions or need assistance, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
            ) + ctaRow("{{loginUrl}}", "Access Your Courses")
    );

    private static final String PACKAGE_ASSIGNED_AND_USER_CREDENTIAL_EMAIL_HTML_TEMPLATE = buildEmail(
            "Welcome to Aspire Tech", "Welcome to Aspire Tech!",
            bodyTd(
                    "<h2 style=\"margin:0 0 20px 0;font-size:18px;font-weight:bold;color:#1a2742;font-family:Arial,Helvetica,sans-serif\">Hi {{userName}},</h2>" +
                            p("Welcome to <strong>Aspire Tech!</strong> We're thrilled to have you on board and look forward to helping your organization succeed with our <strong>Security Awareness Training Portal.</strong>") +
                            p("Your account has been successfully created. Here are your login details:") +
                            infoBox("Account Details",
                                    detail("Login URL", link("{{loginUrl}}", "{{loginUrl}}")) +
                                            detailLast("User ID", "{{email}}")
                            ) +
                            pLast("To secure your account, please set your password using the link below:")
            ) + ctaRow("{{resetUrl}}", "Set My Password") +
                    extraTd(
                            "<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">This password setting link will expire in {{expiryTime}}</p>" +
                                    "<p style=\"margin:0 0 20px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">If the link above doesn't work, copy and paste this URL into your browser:<br />" + link("{{resetUrl}}", "{{resetUrl}}") + "</p>" +
                                    p("Great news! A new course has been assigned to you.") +
                                    successBox("Course Details",
                                            detail("Course Name", "{{packageName}}") +
                                                    detailLast("Assignment Date", "{{assignmentDate}}")
                                    ) +
                                    infoBox("&#128274; Security Information",
                                            detail("Password Setting Link Expires In", "{{expiryTime}}") +
                                                    detailLast("Security Note", "This link will expire after {{expiryTime}} for your security.")
                                    ) +
                                    successBox("Platform Features",
                                            "<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;font-weight:bold;color:#444444\">Top-Rated AI-Powered Human Risk Defense Platform:</p>" +
                                                    ul("Cut phishing clicks 80% in 60 days.", "Improve employee behavior fast with AI.", "Spot fake emails and deepfakes instantly.", "Block ransomware entry points in seconds.", "Train and transform cyber behavior automatically.")
                                    ) +
                                    warningBox("Getting Started", ul(
                                            "Complete your profile setup",
                                            "Explore available courses",
                                            "Track your learning progress",
                                            "Earn certificates upon completion"
                                    )) +
                                    "<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">If you have any questions, feel free to reach out to us at <span style=\"color:#1a6fa0\">+1.917.600.9233</span> or email us at " + mailto("{{supportEmail}}") + "</p>" +
                                    "<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">For more information, visit our <a href=\"https://securityawarenesstraining.ai/company/contact\" style=\"color:#1a6fa0;text-decoration:none\">Help Center</a></p>" +
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777;font-style:italic\">We ensure that all communications are compliant with data protection regulations, including GDPR.</p>"
                    )
    );

    private static final String PACKAGE_ASSIGNED_AND_USER_CREDENTIAL_EMAIL_HTML_TEMPLATE1 = buildEmail(
            "Course Assignment Confirmation - {{packageName}}", "&#128218; Course Assignment Confirmation",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Great news! A new course has been assigned to you.") +
                            successBox("Course Details",
                                    detail("Course Name", "{{packageName}}") +
                                            "<!-- <p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\"><strong>Course Details::</strong> {{packageDetails}}</p>-->" +
                                            detailLast("Assignment Date", "{{assignmentDate}}")
                            ) +
                            infoBox("&#128274; Security Information",
                                    detail("Password Setting Link Expires In", "{{expiryTime}}") +
                                            detailLast("Security Note", "This link will expire after {{expiryTime}} for your security.")
                            ) +
                            infoBox("Your Account Details",
                                    detail("Email", "{{email}}") +
                                            detailLast("Login URL", link("{{loginUrl}}", "{{loginUrl}}"))
                            )
            ) + ctaRow("{{resetUrl}}", "Set My Password") +
                    extraTd(
                            warningBox("&#9888;&#65039; Important", ul("Never share your link with anyone", "For security, please set your password immediately")) +
                                    p("You can also copy and paste this link into your browser:") +
                                    "<p style=\"margin:0 0 20px 0;background-color:#f0f8fd;padding:10px;border-radius:3px;word-break:break-all;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">{{resetUrl}}</p>" +
                                    successBox("What's Next?", ul("Log in to your {{companyName}} account", "Access your assigned courses", "Start your learning journey", "Track your progress")) +
                                    pLast("If you have any questions or need assistance, please don't hesitate to contact our support team at " + mailto("{{supportEmail}}") + ".")
                    )
    );

    private static final String PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE = buildEmail(
            "Course Assignment Confirmation - {{companyName}}", "Course Assignment Confirmation",
            bodyTd(p("Dear {{adminName}},") + p("A course has been assigned to a user. Please review the assignment details below.") +
                    infoBox("Assignment Details",
                            detail("User Name", "{{userName}}") +
                                    detail("Course Name", "{{packageName}}") +
                                    "<!--<p style=\"margin:0 0 10px 0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\"><strong>Course Details:</strong> {{packageDetails}}</p>-->" +
                                    detailLast("Assignment Date", "{{assignmentDate}}")
                    ))
    );

    private static final String PACKAGE_EXPIRY_EMAIL_HTML_TEMPLATE = buildEmail(
            "Package Expiry Alert - {{companyName}}", "Package Expiry Alert",
            bodyTd(p("Dear {{adminName}},") + p("A user's package is nearing its expiration date. Please review the details and take any necessary action.") +
                    warningBox("Expiry Details", detail("User Name", "{{userName}}") + detail("Package Name", "{{packageName}}") + detailLast("Expiration Date", "{{expirationDate}}")))
    );

    private static final String CAMPAIGN_ACTIVITY_EMAIL_HTML_TEMPLATE = buildEmail(
            "Campaign Activity Alert - {{companyName}}", "Campaign Activity Alert",
            bodyTd(p("Dear {{adminName}},") + p("Significant campaign activity has been detected. Please review the details below.") +
                    infoBox("Activity Details", detail("Campaign Type", "{{campaignType}}") + detail("User Engagement", "{{userEngagement}}") + detailLast("Timestamp", "{{timestamp}}")))
    );

    private static final String PAYMENT_SUCCESS_EMAIL_HTML_TEMPLATE = buildEmail(
            "Payment Processed - {{companyName}}", "Payment Processed",
            bodyTd(p("Dear {{adminName}},") + p("A payment has been successfully processed.") +
                    successBox("Payment Details", detail("User Name", "{{userName}}") + detail("Payment Amount", "${{amount}}") + detail("Payment Method", "{{method}}") + detailLast("Timestamp", "{{timestamp}}")))
    );

    private static final String PAYMENT_FAILURE_EMAIL_HTML_TEMPLATE = buildEmail(
            "Payment Failed - {{companyName}}", "Payment Failed",
            bodyTd(p("Dear {{adminName}},") + p("A payment has failed to process. Please review the failure details below and take appropriate action.") +
                    dangerBox("Failure Details", detail("User Name", "{{userName}}") + detail("Failure Reason", "{{failureReason}}") + detail("Payment Amount", "${{amount}}") + detailLast("Timestamp", "{{timestamp}}")))
    );

    private static final String PENDING_PAYMENT_EMAIL_HTML_TEMPLATE = buildEmail(
            "Pending Payment Alert - {{companyName}}", "Pending Payment Alert",
            bodyTd(p("Dear {{adminName}},") + p("A payment is currently pending and requires your attention. Please review the details below.") +
                    warningBox("Payment Details", detail("User Name", "{{userName}}") + detail("Payment Amount", "{{amount}}") + detailLast("Due Date", "{{dueDate}}")))
    );

    private static final String REFUND_REQUEST_EMAIL_HTML_TEMPLATE = buildEmail(
            "Refund Request - {{companyName}}", "Refund Request",
            bodyTd(p("Dear {{adminName}},") + p("A refund request has been submitted and requires your review. Details are provided below.") +
                    infoBox("Request Details", detail("User Name", "{{userName}}") + detail("Refund Reason", "{{refundReason}}") + detailLast("Refund Amount", "{{amount}}")))
    );

    private static final String NEW_POLICY_CREATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "New Policy Created - {{companyName}}", "New Policy Created",
            bodyTd(p("Dear {{adminName}},") + p("A new policy has been successfully created and approved. Please review the details below.") +
                    infoBox("Policy Details", detail("Policy Title", "{{policyTitle}}") + detail("Description", "{{policyDescription}}") + detailLast("Approval Status", "{{approvalStatus}}")))
    );

    private static final String POLICY_UPDATED_EMAIL_HTML_TEMPLATE = buildEmail(
            "Policy Updated - {{companyName}}", "Policy Updated",
            bodyTd(p("Dear {{adminName}},") + p("An existing policy has been updated.") +
                    infoBox("Update Details", detail("Policy Title", "{{policyTitle}}") + detail("Updated Fields", "{{updatedFields}}") + detailLast("Update Timestamp", "{{timestamp}}")))
    );

    private static final String POLICY_COMPLIANCE_REMINDER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Policy Compliance Reminder - {{companyName}}", "Policy Compliance Reminder",
            bodyTd(p("Dear {{adminName}},") + p("This is a reminder to review or update the following policy for compliance.") +
                    warningBox("Reminder Details", detail("Policy Title", "{{policyTitle}}") + detailLast("Reminder Date", "{{reminderDate}}")))
    );

    private static final String CERTIFICATE_ISSUED_USER_EMAIL_HTML_TEMPLATE = buildEmail(
            "&#127891; Your Certificate Has Been Issued – {{courseTitle}}", "&#127891; Your Certificate Has Been Issued",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("Congratulations! &#127881;<br />We're pleased to inform you that your certificate for \"<strong>{{courseTitle}}</strong>\" has been successfully issued.") +
                            successBox("Certificate Details", detail("Course Title", "{{courseTitle}}") + detailLast("Issue Date", "{{issueDate}}")) +
                            pLast("You can now download or view your certificate from your {{companyName}} account. Thank you for your dedication and effort — we're proud to be part of your learning journey!")
            ) + ctaRow("{{loginUrl}}", "View Your Certificate")
    );

    private static final String CERTIFICATE_ISSUED_ADMIN_EMAIL_HTML_TEMPLATE = buildEmail(
            "Certificate Issued - {{companyName}}", "Certificate Issued",
            bodyTd(p("Dear {{adminName}},") + p("A certificate has been successfully issued to a user. Details are provided below.") +
                    successBox("Certificate Details", detail("User Name", "{{userName}}") + detail("Course Title", "{{courseTitle}}") + detailLast("Issue Date", "{{issueDate}}")))
    );

    private static final String LEADERBOARD_UPDATE_EMAIL_HTML_TEMPLATE = buildEmail(
            "Leaderboard Updated - {{companyName}}", "Leaderboard Updated",
            bodyTd(p("Dear {{adminName}},") + p("The leaderboard has been updated with new top performers.") +
                    infoBox("Update Details", detail("Top Performers", "{{topPerformers}}") + detailLast("Update Timestamp", "{{timestamp}}")))
    );

    private static final String CERTIFICATE_EXPIRY_EMAIL_HTML_TEMPLATE = buildEmail(
            "Certificate Expiry Alert - {{companyName}}", "Certificate Expiry Alert",
            bodyTd(p("Dear {{adminName}},") + p("A certificate is approaching its expiration date and may require renewal. Please review the details below.") +
                    warningBox("Expiry Details", detail("User Name", "{{userName}}") + detail("Certificate Title", "{{certificateTitle}}") + detailLast("Expiration Date", "{{expirationDate}}")))
    );

    private static final String CERTIFICATE_EXPIRING_EMAIL_HTML_TEMPLATE = buildEmail(
            "Certificate Expiring Reminder - {{companyName}}", "Certificate Expiring Reminder",
            bodyTd(
                    p("Dear {{adminName}},") +
                            p("This is a friendly reminder that several of your team members' Security Awareness Training Certificates will be expiring on <strong>{{expirationDate}}</strong>.") +
                            p("As the Client Admin, we kindly ask for your support in ensuring that your team members complete the necessary actions to renew or recertify their training before the expiration date.") +
                            infoBox("Next Steps", ol(
                                    "<strong>Log in to your Aspire Tech Portal/Training Portal:</strong> " + link("{{portalURL}}", "{{portalURL}}"),
                                    "<strong>Review Team's Training Status:</strong> Go to the Certifications section to view your users' current certificate statuses.",
                                    "<strong>Encourage Action:</strong> Please remind your team members to complete any required training or recertification steps before their certificates expire."
                            )) +
                            warningBox("&#128206; Important",
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">Please open the attached CSV/Excel file for a detailed list of users with expiring certificates. The file contains all the necessary information and actions for renewal.</p>"
                            ) +
                            p("If you need any assistance or have questions about the renewal process, please feel free to contact our support team at " + mailto("{{supportEmail}}") + ".") +
                            pLast("Thank you for your continued partnership in maintaining a secure and compliant environment!")
            ), ASPIRE_TEAM_SIGNOFF
    );

    private static final String SYSTEM_DOWNTIME_EMAIL_HTML_TEMPLATE = buildEmail(
            "System Downtime Alert - {{companyName}}", "System Downtime Alert",
            bodyTd(p("Dear {{adminName}},") + p("Please be advised that the system is currently experiencing downtime. Details are provided below.") +
                    dangerBox("Downtime Details", detail("Downtime Reason", "{{reason}}") + detail("Start Time", "{{startTime}}") + detailLast("End Time", "{{endTime}}")))
    );

    private static final String SECURITY_ALERT_EMAIL_HTML_TEMPLATE = buildEmail(
            "Verification Code - {{companyName}}", "&#128274; Verification Code",
            bodyTd(
                    p("Dear{{userName}},") +
                            p("You have requested a verification code for your {{companyName}} account. Please use the code below to complete your verification.") +
                            codeBox(
                                    "<p style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#777777;margin:0 0 10px 0;text-transform:uppercase;letter-spacing:1px\">Your Verification Code</p>" +
                                            "<p style=\"font-size:36px;font-weight:bold;color:#42b9ea;margin:10px 0;letter-spacing:8px;font-family:'Courier New',Courier,monospace\">{{otp}}</p>" +
                                            "<p style=\"font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777;margin:10px 0 0 0\">This code will expire in {{expiryMinutes}} minutes</p>"
                            ) +
                            warningBox("&#9888;&#65039; Security Notice", ul(
                                    "Never share this code with anyone",
                                    "{{companyName}} staff will never ask for your verification code",
                                    "If you didn't request this code, please ignore this email",
                                    "This code expires in {{expiryMinutes}} minutes for your security"
                            )) +
                            pLast("If you have any questions or concerns, please contact our support team at " + mailto("{{supportEmail}}") + ".")
            )
    );

    private static final String UPDATES_PATCHES_EMAIL_HTML_TEMPLATE = buildEmail(
            "System Updates Available - {{companyName}}", "System Updates Available",
            bodyTd(p("Dear {{adminName}},") + p("New updates and patches are available for the portal. Please review the release details below.") +
                    infoBox("Update Details", detail("Patch/Update Version", "{{version}}") + detailLast("Release Date", "{{releaseDate}}")))
    );

    private static final String FEATURE_UPDATE_EMAIL_HTML_TEMPLATE = buildEmail(
            "Feature Update - {{companyName}}", "Feature Update",
            bodyTd(p("Dear {{adminName}},") + p("A new feature has been released or an existing feature has been updated. Please review the details below.") +
                    infoBox("Feature Details", detail("Feature Name", "{{featureName}}") + detailLast("Change Details", "{{changeDetails}}")))
    );

    private static final String DOMAIN_VERIFICATION_EMAIL_HTML_TEMPLATE = buildEmail(
            "Domain Verification Code - {{domainName}}", "Domain Verification",
            bodyTd(
                    p("Hello,") +
                            p("You have requested to verify ownership of the domain <strong>{{domainName}}</strong>.") +
                            p("Please use the following 6-digit verification code to complete the process:") +
                            codeBox("<span style=\"font-size:32px;font-weight:bold;letter-spacing:6px;color:#42b9ea;font-family:'Courier New',Courier,monospace\">{{verificationCode}}</span>") +
                            warningBox("Important",
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">This code will expire in <strong>{{expiryTime}}</strong>. If it expires, you will need to request a new one.</p>"
                            ) +
                            pLast("If you did not request this verification, please ignore this email.")
            )
    );

    private static final String PENDING_TASKS_REMINDER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Overdue Tasks Reminder - {{companyName}}", "Overdue Tasks Reminder",
            bodyTd(p("Dear {{adminName}},") + p("You have overdue tasks that require your immediate attention. Please review and take action.") +
                    warningBox("Task Details", detail("Task Type", "{{taskType}}") + detail("Task Description", "{{taskDescription}}") + detailLast("Due Date", "{{dueDate}}")))
    );

    private static final String SCHEDULED_MAINTENANCE_EMAIL_HTML_TEMPLATE = buildEmail(
            "Scheduled Maintenance - {{companyName}}", "Scheduled Maintenance",
            bodyTd(p("Dear {{adminName}},") + p("System maintenance is scheduled as follows:") +
                    infoBox("Maintenance Details", detail("Maintenance Type", "{{maintenanceType}}") + detailLast("Scheduled Date", "{{scheduledDate}}")))
    );

    private static final String HELP_DESK_TICKET_EMAIL_HTML_TEMPLATE = buildEmail(
            "Help Desk Ticket Update - {{companyName}}", "Help Desk Ticket Update",
            bodyTd(p("Dear {{adminName}},") + p("A help desk ticket has been updated. Please review the current status below.") +
                    infoBox("Ticket Details", detail("Ticket ID", "{{ticketId}}") + detail("Ticket Status", "{{status}}") + detailLast("Resolution Date", "{{resolutionDate}}")))
    );

    private static final String SUBSCRIPTION_RENEWAL_EMAIL_HTML_TEMPLATE = buildEmail(
            "Subscription Renewal Reminder - {{companyName}}", "Subscription Renewal Reminder",
            bodyTd(p("Dear {{adminName}},") + p("Your portal subscription is approaching its renewal date. Please take action to avoid any service interruption.") +
                    warningBox("Subscription Details", detail("Subscription Name", "{{subscriptionName}}") + detailLast("Renewal Date", "{{renewalDate}}")))
    );

    private static final String CLIENT_ADMIN_PACKAGE_ASSIGNED_EMAIL_HTML_TEMPLATE = buildEmail(
            "Packages Assigned - {{companyName}}", "Packages Assigned To Your Organization",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("New package assignments have been made to your organization account on <strong>{{assignmentDate}}</strong>.") +
                            infoBox("Assignment Summary", "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">{{packageDetails}}</p>") +
                            pLast("You can review the assigned packages and manage licenses from your dashboard.")
            ) + ctaRow("{{loginUrl}}", "Go to Dashboard") +
                    extraTd(
                            "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:12px;color:#777777\">If you need assistance, please contact our support at " + mailto("{{supportEmail}}") + ".</p>"
                    )
    );

    private static final String COURSE_NOT_STARTED_USER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Action Required: You Have Not Started the Assigned Course - {{companyName}}", "Action Required: You Have Not Started the Assigned Course",
            bodyTd(
                    p("Dear {{userName}},") +
                            p("This is a reminder that you have been assigned a course under the sub-package <strong>{{subPackageName}}</strong>. According to our records, you have not yet started the course.") +
                            warningBox("Important Reminder",
                                    "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#444444\">As part of the training process, it is important that you begin the course within the next 7 days to ensure timely completion.</p>"
                            ) +
                            pLast("Please log in to your portal to begin the course as soon as possible. If you need assistance, feel free to contact " + mailto("{{supportEmail}}") + ".")
            ) + ctaRow("{{loginUrl}}", "Access Your Course") +
                    extraTd(p("Thank you for your attention.")),
            "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">{{companyName}}</span><br />" + mailto("{{supportEmail}}")
    );

    private static final String COURSE_NOT_STARTED_MANAGER_EMAIL_HTML_TEMPLATE = buildEmail(
            "Reminder: Employee Has Not Started the Assigned Course - {{companyName}}", "Reminder: Employee Has Not Started the Assigned Course",
            bodyTd(
                    p("Dear {{managerName}},") +
                            p("This is a follow-up regarding the course assigned to your team member, <strong>{{userName}}</strong>, under the sub-package <strong>{{subPackageName}}</strong>. As of today, they have not started the course.") +
                            infoBox("Employee Details", detail("Employee Name", "{{userName}}") + detail("Course/Sub-Package", "{{subPackageName}}") + detailLast("Assignment Date", "{{assignmentDate}}")) +
                            p("Please reach out to <strong>{{userName}}</strong> to ensure they begin the course promptly and stay on track with their training.") +
                            p("If you need any assistance or further information, feel free to contact " + mailto("{{supportEmail}}") + ".") +
                            pLast("Thank you for your attention to this matter.")
            ), "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">{{companyName}}</span><br />" + mailto("{{supportEmail}}")
    );

    private static final String MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL_EMAIL_HTML_TEMPLATE = buildEmail(
            "Mandatory Security Awareness Training Assigned - {{companyName}}", "Mandatory Security Awareness Training Assigned",
            bodyTd(
                    p("Dear {{receiverName}},") +
                            p("We would like to inform you that <strong>{{userName}}</strong> from the <strong>{{departmentName}}</strong> has been assigned the mandatory security awareness training course. The completion of this course is required to ensure compliance with company policies and to enhance our overall security posture.") +
                            infoBox("Employee Details",
                                    detail("Name", "{{userName}}") + detail("Department", "{{departmentName}}") + detail("Course/Sub-Package", "{{courseName}}") + detail("Assignment Date", "{{assignmentDate}}") + detailLast("Expiration Date", "{{expirationDate}}")
                            ) +
                            p("As part of their ongoing development, it is important that <strong>{{userName}}</strong> complete this training by the assigned expiration date. We encourage you to monitor their progress and provide any support as needed to ensure timely completion.") +
                            p("Should you have any questions or need assistance with the training platform, feel free to reach out to our support team at " + mailto("{{supportEmail}}") + ".") +
                            pLast("Thank you for your attention and for supporting our commitment to security and compliance.")
            ), "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">The ASAT Learning Platform</span>"
    );

    private static final String COURSE_NOT_STARTED_C_LEVEL_EMAIL_HTML_TEMPLATE = buildEmail(
            "Urgent: Employee Has Not Started the Assigned Course - {{companyName}}", "Urgent: Employee Has Not Started the Assigned Course",
            bodyTd(
                    p("Dear {{receiverName}},") +
                            p("We wanted to inform you that your team member, <strong>{{userName}}</strong>, has still not started the course under the sub-package <strong>{{subPackageName}}</strong>, which was assigned <strong>{{daysSinceAssignment}} days ago</strong>. As this course is critical to their professional development, we recommend that immediate action be taken to address this issue.") +
                            dangerBox("&#9888;&#65039; Urgent Action Required",
                                    detail("Employee Name", "{{userName}}") + detail("Course/Sub-Package", "{{subPackageName}}") + detail("Days Since Assignment", "{{daysSinceAssignment}} days") + detailLast("Assignment Date", "{{assignmentDate}}")
                            ) +
                            p("Please liaise with the respective Manager to ensure the course is completed on time.") +
                            pLast("If you require further information or assistance, feel free to contact " + mailto("{{supportEmail}}") + ".")
            ), "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">{{companyName}}</span><br />" + mailto("{{supportEmail}}")
    );

    private static final String COURSE_EXPIRY_HR_EMAIL_HTML_TEMPLATE = buildEmail(
            "Final Reminder: Course Expiration - {{companyName}}", "Final Reminder: Course Expiration",
            bodyTd(
                    p("Dear {{receiverName}},") +
                            p("This is a final reminder regarding the course assigned to <strong>{{userName}}</strong> under the sub-package <strong>{{subPackageName}}</strong>. The course will expire in <strong>2 days</strong>, and it has not yet been completed.") +
                            dangerBox("&#9888;&#65039; Final Reminder",
                                    detail("Employee Name", "{{userName}}") + detail("Course/Sub-Package", "{{subPackageName}}") + detail("Expiry Date", "{{expiryDate}}") + detail("Days Remaining", "2 days") + detailLast("Status", "Not Completed")
                            ) +
                            p("Please reach out to the employee and their respective manager to ensure the course is completed before the expiration date.") +
                            p("If you need any further assistance or have questions, please contact " + mailto("{{supportEmail}}") + ".") +
                            pLast("Thank you for your attention.")
            ), "Best regards,<br /><span style=\"font-weight:700;color:#1a2742\">{{companyName}}</span><br />" + mailto("{{supportEmail}}")
    );

    // ── Runtime Methods ──────────────────────────────────────────────────

    @Override
    public void run(String... args) {
        if (!dataProperties.isInitializeOnStartup()) {
            log.info("ℹ️ Data initialization is disabled via configuration");
            return;
        }

        log.info("🚀 Starting notification system data initialization...");

        try {
            initializeNotificationSettings();
            initializeRoleSettings();
            initializeNotificationTemplates();

            log.info("✅ Notification system data initialization completed successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to initialize notification system data: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Initialize notification settings for all notification types
     */
    private void initializeNotificationSettings() {
        log.info("📊 Initializing notification settings...");

        // Always sync settings - check each one individually to prevent duplicates
        // This ensures new notification types are automatically added on startup
        List<NotificationSettings> settings = Arrays.asList(
                // User Management
                createNotificationSetting(NotificationType.NEW_USER_REGISTERED, "Sent when a new user registers on the platform"),
                createNotificationSetting(NotificationType.WELCOME_EMAIL, "Sent as a welcome email to new users after registration"),
                createNotificationSetting(NotificationType.WELCOME_EMAIL_CLIENT_USER, "Sent as a welcome email to new users after registration"),
                createNotificationSetting(NotificationType.WELCOME_CLIENT_EMAIL, "Sent as a welcome email to new client admin after registration"),
                createNotificationSetting(NotificationType.WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD, "Sent as a welcome email to new client admin after buy now or trial registration"),
                createNotificationSetting(NotificationType.USER_PROFILE_UPDATED, "Sent when a user updates their profile information"),
                createNotificationSetting(NotificationType.PASSWORD_RESET_REQUEST, "Sent when a user requests password reset"),
                createNotificationSetting(NotificationType.PASSWORD_RESET_BY_ASPIRE, "Sent when a admin requests password reset"),
                createNotificationSetting(NotificationType.USER_SUSPENDED, "Sent when a user account is suspended or deactivated"),
                createNotificationSetting(NotificationType.USER_SUSPENSION, "Sent when a user account is suspended or deactivated"),
                createNotificationSetting(NotificationType.USER_ACTIVATED, "Sent when a user account is reactivated"),
                createNotificationSetting(NotificationType.USER_PASSWORD_CHANGE, "Sent to user when their password is changed"),
                createNotificationSetting(NotificationType.USER_PASSWORD_CHANGE_ADMIN, "Sent to admin when a user changes their password"),
                createNotificationSetting(NotificationType.BULK_USER_IMPORT_SUMMARY, "Sent to admin when bulk user import is completed"),

                // Content Management
                createNotificationSetting(NotificationType.NEW_COURSE_CREATED, "Sent when a new course is created"),
                createNotificationSetting(NotificationType.COURSE_UPDATED, "Sent when a course is updated"),
                createNotificationSetting(NotificationType.COURSE_ASSIGNED, "Sent when a course is assigned to a user"),
                createNotificationSetting(NotificationType.COURSE_COMPLETION, "Sent when a user completes a course"),
                createNotificationSetting(NotificationType.COURSE_COMPLETION_USER, "Sent to user when they complete a course"),
                createNotificationSetting(NotificationType.COURSE_NOT_STARTED_USER, "Sent to user when they haven't started an assigned course"),
                createNotificationSetting(NotificationType.COURSE_NOT_STARTED_MANAGER, "Sent to manager when employee hasn't started assigned course"),
                createNotificationSetting(NotificationType.COURSE_NOT_STARTED_C_LEVEL, "Sent to C-level executives when employee hasn't started course after 21 days"),
                createNotificationSetting(NotificationType.COURSE_EXPIRY_HR, "Sent to HR when course is about to expire in 2 days"),

                // Package Management
                createNotificationSetting(NotificationType.NEW_PACKAGE_CREATED, "Sent when a new package is created"),
                createNotificationSetting(NotificationType.PACKAGE_ASSIGNED, "Sent to admin when a package is assigned to a user"),
                createNotificationSetting(NotificationType.PACKAGE_ASSIGNED_USER, "Sent to user when a package is assigned to them"),
                createNotificationSetting(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, "Sent to user when a package is 1st time assigned to them"),
                createNotificationSetting(NotificationType.PACKAGE_EXPIRY, "Sent when a package is about to expire"),
                createNotificationSetting(NotificationType.CAMPAIGN_ACTIVITY, "Sent for campaign-related activities"),
                createNotificationSetting(NotificationType.CLIENT_ADMIN_PACKAGE_ASSIGNED, "Sent to client admin when packages are assigned"),
                createNotificationSetting(NotificationType.MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL, "Sent to Manager, C-Level, and HR when mandatory training is assigned to end users"),

                // Payment Management
                createNotificationSetting(NotificationType.PAYMENT_SUCCESS, "Sent when a payment is successful"),
                createNotificationSetting(NotificationType.PAYMENT_FAILURE, "Sent when a payment fails"),
                createNotificationSetting(NotificationType.PENDING_PAYMENT, "Sent for pending payments"),
                createNotificationSetting(NotificationType.REFUND_REQUESTED, "Sent when a refund is requested"),

                // Policy Management
                createNotificationSetting(NotificationType.NEW_POLICY_CREATED, "Sent when a new policy is created"),
                createNotificationSetting(NotificationType.POLICY_UPDATED, "Sent when a policy is updated"),
                createNotificationSetting(NotificationType.POLICY_COMPLIANCE_REMINDER, "Sent as a reminder for policy compliance"),

                // Certificate Management
                createNotificationSetting(NotificationType.CERTIFICATE_ISSUED, "Sent when a certificate is issued"),
                createNotificationSetting(NotificationType.CERTIFICATE_ISSUED_ADMIN, "Sent to admin when a certificate is issued to a user"),
                createNotificationSetting(NotificationType.LEADERBOARD_UPDATE, "Sent when leaderboard is updated"),
                createNotificationSetting(NotificationType.CERTIFICATE_EXPIRY, "Sent when a certificate is about to expire"),
                createNotificationSetting(NotificationType.CERTIFICATE_EXPIRING, "Sent to client admin when team members' certificates are expiring with Excel attachment"),
                createNotificationSetting(NotificationType.CERTIFICATE_REVOKED, "Sent when a certificate is revoked"),

                // System/Operational
                createNotificationSetting(NotificationType.SYSTEM_HEALTH_ALERTS, "Sent for system health alerts"),
                createNotificationSetting(NotificationType.SECURITY_ALERTS, "Sent for security alerts"),
                createNotificationSetting(NotificationType.UPDATES_PATCHES, "Sent for system updates and patches"),
                createNotificationSetting(NotificationType.FEATURE_UPDATE_CHANGE, "Sent for feature updates and changes"),
                createNotificationSetting(NotificationType.DOMAIN_VERIFICATION, "Sent for domain ownership verification codes"),

                // General
                createNotificationSetting(NotificationType.REMINDER_PENDING_TASKS, "Sent as reminder for pending tasks"),
                createNotificationSetting(NotificationType.SCHEDULED_MAINTENANCE, "Sent for scheduled system maintenance"),
                createNotificationSetting(NotificationType.HELP_DESK_TICKET_UPDATES, "Sent for help desk ticket updates"),
                createNotificationSetting(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, "Sent for subscription renewal reminders")
        );

        // Fetch all existing settings once to avoid individual database calls
        Set<NotificationType> existingTypes = settingsRepository.findAll().stream()
                .map(NotificationSettings::getNotificationType)
                .collect(Collectors.toSet());

        int inserted = 0;
        int skipped = 0;
        for (NotificationSettings setting : settings) {
            if (!existingTypes.contains(setting.getNotificationType())) {
                settingsRepository.save(setting);
                inserted++;
                log.info("✅ Initialized new notification settings for type: {}", setting.getNotificationType());
            } else {
                skipped++;
                log.debug("⏭️  Notification settings already exist for type: {}, skipping", setting.getNotificationType());
            }
        }
        log.info("✅ Notification settings sync complete. Inserted: {}. Skipped (already exist): {}. Total in database: {}.",
                inserted, skipped, settingsRepository.count());

        reconcileGlobalChannelWidening();
    }

    /**
     * Create a notification setting with the default configuration
     */
    private NotificationSettings createNotificationSetting(NotificationType type, String description) {
        // Special handling for SECURITY_ALERTS - enable phone_call for MFA
        boolean phoneCallEnabled = type == NotificationType.SECURITY_ALERTS;
        boolean smsEnabled = phoneCallEnabled || SMS_ENABLED_GLOBAL_TYPES.contains(type);

        return NotificationSettings.builder()
                .notificationType(type)
                .enabled(true)
                .description(description)
                .emailEnabled(true)
                .inAppEnabled(true)
                .smsEnabled(smsEnabled)
                .pushEnabled(false)
                .phoneCallEnabled(phoneCallEnabled)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Widen the SMS channel flag on any pre-existing global notification settings so
     * environments deployed before the MSP role scope matrix was introduced pick up the
     * channels it requires. Only ever widens (turns SMS on); never narrows a channel an
     * admin may have already turned on manually.
     */
    private void reconcileGlobalChannelWidening() {
        List<NotificationSettings> existingSettings = settingsRepository.findAll();
        int widened = 0;
        for (NotificationSettings existing : existingSettings) {
            if (SMS_ENABLED_GLOBAL_TYPES.contains(existing.getNotificationType()) && !existing.isSmsEnabled()) {
                existing.setSmsEnabled(true);
                existing.setUpdatedAt(LocalDateTime.now());
                settingsRepository.save(existing);
                widened++;
            }
        }
        if (widened > 0) {
            log.info("🔧 Widened SMS channel on {} pre-existing notification settings to match the role scope matrix", widened);
        }
    }

    /**
     * Seed the role-based notification settings matrix (Aspire Admin, MSP, Client Admin, User)
     * for every notification type. Only inserts missing (type, role) rows; never overwrites
     * admin-edited settings. USER and CLIENT_ADMIN default to enabled; MSP and ASPIRE_ADMIN
     * default to disabled (opt-in via Admin Portal) while keeping channel flags ready for when
     * a type is turned on.
     */
    private void initializeRoleSettings() {
        log.info("🧩 Initializing role-based notification settings matrix...");

        long before = countRoleSettingsSafely();
        for (NotificationType type : NotificationType.values()) {
            for (NotificationRecipientRole role : NotificationRecipientRole.values()) {
                RoleChannelDefaults defaults = defaultsForRole(role, type);
                roleSettingsService.seedIfMissing(type, role, defaults.enabled(), defaults.email(),
                        defaults.inApp(), defaults.sms(), defaults.push(), defaults.phoneCall());
            }
        }
        long after = countRoleSettingsSafely();
        log.info("✅ Role notification settings sync complete. Inserted: {}. Total in database: {}.",
                after - before, after);
    }

    private long countRoleSettingsSafely() {
        return roleSettingsService.getMatrix().size();
    }

    private RoleChannelDefaults defaultsForRole(NotificationRecipientRole role, NotificationType type) {
        return switch (role) {
            case MSP -> mspDefaultsFor(type);
            case ASPIRE_ADMIN -> aspireAdminDefaultsFor(type);
            case USER, CLIENT_ADMIN -> defaultRoleDefaultsFor(type);
        };
    }

    /**
     * Default role scope for USER and CLIENT_ADMIN: enabled, mirroring the global channel
     * flags for the type until dedicated per-role matrices are supplied.
     */
    private RoleChannelDefaults defaultRoleDefaultsFor(NotificationType type) {
        boolean phoneCallEnabled = type == NotificationType.SECURITY_ALERTS;
        boolean smsEnabled = phoneCallEnabled || SMS_ENABLED_GLOBAL_TYPES.contains(type);
        return new RoleChannelDefaults(true, true, true, smsEnabled, false, phoneCallEnabled);
    }

    /**
     * Aspire Admin defaults to disabled (opt-in via Admin Portal). Channel flags mirror
     * {@link #defaultRoleDefaultsFor(NotificationType)} so enabling a type in the portal
     * already has sensible email / in-app / SMS settings.
     */
    private RoleChannelDefaults aspireAdminDefaultsFor(NotificationType type) {
        boolean phoneCallEnabled = type == NotificationType.SECURITY_ALERTS;
        boolean smsEnabled = phoneCallEnabled || SMS_ENABLED_GLOBAL_TYPES.contains(type);
        return new RoleChannelDefaults(false, true, true, smsEnabled, false, phoneCallEnabled);
    }

    /**
     * MSP channel scope per the notification scope matrix; {@code enabled} is always false
     * so MSP is opt-in via Admin Portal.
     */
    private RoleChannelDefaults mspDefaultsFor(NotificationType type) {
        if (MSP_DISABLED_TYPES.contains(type)) {
            return new RoleChannelDefaults(false, true, true, false, false, false);
        }
        boolean email = !MSP_EMAIL_OFF_TYPES.contains(type);
        boolean inApp = !MSP_IN_APP_OFF_TYPES.contains(type);
        boolean sms = SMS_ENABLED_GLOBAL_TYPES.contains(type);
        return new RoleChannelDefaults(false, email, inApp, sms, false, false);
    }

    /**
     * Channel defaults used while seeding a role settings row.
     */
    private record RoleChannelDefaults(boolean enabled, boolean email, boolean inApp, boolean sms, boolean push, boolean phoneCall) {
    }

    /**
     * Seed notification templates. Inserts missing templates only; never overwrites
     * existing records (including admin-edited content).
     */
    private void initializeNotificationTemplates() {
        log.info("📝 Initializing notification templates...");

        List<NotificationTemplate> templates = Arrays.asList(
                // NEW_USER_REGISTERED Templates
                createEmailTemplate(
                        NotificationType.NEW_USER_REGISTERED,
                        "New User Registered Email Template",
                        "New User Registration - {{companyName}}",
                        NEW_USER_REGISTERED_EMAIL_HTML_TEMPLATE,
                        "Dear {{adminName}}, We would like to inform you that a new user has successfully registered in the system. - User Name: {{userName}} - User Email: {{userEmail}} - Registration Timestamp: {{timestamp}} Best regards, {{companyName}}"
                ),
                createInAppTemplate(
                        NotificationType.NEW_USER_REGISTERED,
                        "New User Registered In-App Template",
                        "New User Registered",
                        "A new user {{userName}} has registered in the system."
                ),


                // WELCOME_EMAIL Templates
                createEmailTemplate(
                        NotificationType.WELCOME_EMAIL_CLIENT_USER,
                        "Welcome Email Template",
                        "Welcome to {{companyName}}!",
                        WELCOME_EMAIL_WITHOUT_PASSWORD_HTML_TEMPLATE,
                        "Dear {{userName}}, Welcome to {{companyName}}! Thank you for joining our platform. - Email: {{email}} - Platform Features: {{platformFeatures}} - Support Contact: {{supportEmail}} - Best regards, {{companyName}}"
                ),
                // WELCOME_EMAIL Templates
                createEmailTemplate(
                        NotificationType.WELCOME_EMAIL,
                        "Welcome Email Template",
                        "Welcome to {{companyName}}!",
                        WELCOME_EMAIL_HTML_TEMPLATE,
                        "Dear {{userName}}, Welcome to {{companyName}}! Thank you for joining our platform. - Email: {{email}} - Platform Features: {{platformFeatures}} - Support Contact: {{supportEmail}} - Login URL: {{loginUrl}} Best regards, {{companyName}}"
                ),
                createInAppTemplate(
                        NotificationType.WELCOME_EMAIL,
                        "Welcome Email In-App Template",
                        "Welcome to {{companyName}}!",
                        "Welcome {{userName}}! We're excited to have you on board. Explore our platform features and get started with your learning journey."
                ),

                // WELCOME_EMAIL Templates (admin-side roles) - reuses NEW_USER_REGISTERED copy.
                // No MSP in-app row: WELCOME_EMAIL is in MSP_IN_APP_OFF_TYPES.
                createEmailTemplate(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.CLIENT_ADMIN, "Welcome Email Client Admin Email Template", "New User Registration - {{companyName}}", NEW_USER_REGISTERED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, We would like to inform you that a new user has successfully registered in the system. - User Name: {{userName}} - User Email: {{userEmail}} - Registration Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createEmailTemplate(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.MSP, "Welcome Email MSP Email Template", "New User Registration - {{companyName}}", NEW_USER_REGISTERED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, We would like to inform you that a new user has successfully registered in the system. - User Name: {{userName}} - User Email: {{userEmail}} - Registration Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createEmailTemplate(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.ASPIRE_ADMIN, "Welcome Email Aspire Admin Email Template", "New User Registration - {{companyName}}", NEW_USER_REGISTERED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, We would like to inform you that a new user has successfully registered in the system. - User Name: {{userName}} - User Email: {{userEmail}} - Registration Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.CLIENT_ADMIN, "Welcome Email Client Admin In-App Template", "New User Registered", "A new user {{userName}} has registered in the system."),
                createInAppTemplate(NotificationType.WELCOME_EMAIL, NotificationRecipientRole.ASPIRE_ADMIN, "Welcome Email Aspire Admin In-App Template", "New User Registered", "A new user {{userName}} has registered in the system."),

                // WELCOME_CLIENT_EMAIL Templates
                createEmailTemplate(
                        NotificationType.WELCOME_CLIENT_EMAIL,
                        "Welcome Email Template",
                        "Welcome to Aspire Tech!",
                        WELCOME_CLIENT_EMAIL_HTML_TEMPLATE,
                        "Dear {{userName}}, Welcome to Aspire Tech! We are thrilled to have you on board and look forward to working together to help your organization succeed with our Security Awareness Training Portal. Your account has been successfully created! Please find your login details below: Login URL: {{loginUrl}} User ID: {{email}} Temporary Password: {{password}} Important: Your Account Security is Our Priority! For your protection, you must log in immediately using the credentials above and change your temporary password to a more secure one. To change your password: 1. Log in using the provided temporary password. 2. Navigate to your account settings and select \"Change Password.\" 3. Choose a strong, unique password (we recommend using a combination of letters, numbers, and symbols to strengthen your security). Once you've updated your password, you'll have full access to your account and all the features of our Security Awareness Training Portal. If you encounter any issues or need assistance, our support team is available to help. Feel free to contact us at {{supportEmail}}. Thank you for choosing Aspire Tech. We are committed to ensuring your success and security throughout your journey with us! Best regards, The Aspire Tech Team"
                ),
                createInAppTemplate(
                        NotificationType.WELCOME_CLIENT_EMAIL,
                        "Welcome Email In-App Template",
                        "Welcome to Aspire Tech!",
                        "Welcome {{userName}}! We're excited to have you on board. Explore our platform features and get started with your learning journey."
                ),

                // WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD_HTML_TEMPLATE Templates
                createEmailTemplate(
                        NotificationType.WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD,
                        "Welcome Email Template",
                        "Welcome to Aspire Tech!",
                        WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD_HTML_TEMPLATE,
                        "Dear {{userName}}, Welcome to Aspire Tech! We are thrilled to have you on board and look forward to working together to help your organization succeed with our Security Awareness Training Portal. Your account has been successfully created! Please find your login details below: Login URL: {{loginUrl}} User ID: {{email}} Temporary Password: {{password}} Important: Your Account Security is Our Priority! For your protection, you must log in immediately using the credentials above and change your temporary password to a more secure one. To change your password: 1. Log in using the provided temporary password. 2. Navigate to your account settings and select \"Change Password.\" 3. Choose a strong, unique password (we recommend using a combination of letters, numbers, and symbols to strengthen your security). Once you've updated your password, you'll have full access to your account and all the features of our Security Awareness Training Portal. If you encounter any issues or need assistance, our support team is available to help. Feel free to contact us at {{supportEmail}}. Thank you for choosing Aspire Tech. We are committed to ensuring your success and security throughout your journey with us! Best regards, The Aspire Tech Team"
                ),

                createInAppTemplate(
                        NotificationType.WELCOME_CLIENT_EMAIL_WITHOUT_PASSWORD,
                        "Welcome Email In-App Template",
                        "Welcome to Aspire Tech!",
                        "Welcome {{userName}}! We're excited to have you on board. Explore our platform features and get started with your learning journey."
                ),

                // PASSWORD_RESET_REQUEST Templates
                createEmailTemplate(NotificationType.PASSWORD_RESET_REQUEST, "Password Reset Request Email Template", "Reset Your Password - {{companyName}}", PASSWORD_RESET_REQUEST_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, We received a request to reset your password. Click this link to reset: {{resetUrl}} This link expires in {{expiryTime}}. If you didn't request this, please ignore this email. Best regards, The {{companyName}} Team"),
                createInAppTemplate(NotificationType.PASSWORD_RESET_REQUEST, "Password Reset Request In-App Template", "Password Reset Requested", "Password reset link sent to your email. Check your inbox."),

                // PASSWORD_RESET_BY_ASPIRE Templates
                createEmailTemplate(NotificationType.PASSWORD_RESET_BY_ASPIRE, "Password Reset by Aspire Email Template", "Your New Password - {{companyName}}", PASSWORD_RESET_BY_ASPIRE_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Your password for your {{companyName}} account has been reset by an administrator. Your new password is: {{password}} Please use this password to log in at {{loginUrl}} and change it immediately for your security. If you didn't request this password reset, please contact support immediately at {{supportEmail}}. Best regards, The {{companyName}} Team"),
                createInAppTemplate(NotificationType.PASSWORD_RESET_BY_ASPIRE, "Password Reset Request In-App Template", "Password Reset Requested", "A password reset has been initiated for {{userName}}."),

                // USER_PROFILE_UPDATED Templates
                createEmailTemplate(NotificationType.USER_PROFILE_UPDATED, "User Profile Updated Email Template", "User Profile Updated - {{companyName}}", USER_PROFILE_UPDATED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, This is to inform you that a user profile has been updated. User Name: {{userName}} - Updated Fields: {{updatedFields}} - Update Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.USER_PROFILE_UPDATED, "User Profile Updated In-App Template", "Profile Updated", "User {{userName}}'s profile has been updated."),

                // USER_SUSPENDED Templates
                createEmailTemplate(NotificationType.USER_SUSPENDED, "User Suspended Email Template", "User Account Suspended - {{companyName}}", USER_SUSPENDED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user account has been suspended due to a policy violation or inactivity. User Name: {{userName}} - Reason: {{reason}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.USER_SUSPENDED, "User Suspended In-App Template", "Account Suspended", "User {{userName}}'s account has been suspended due to {{reason}}."),

                // USER_SUSPENSION Templates
                createEmailTemplate(NotificationType.USER_SUSPENSION, "User Suspended Email Template", "User Account Suspended - {{userName}}", USER_SUSPENSION_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Your account has been suspended due to a policy violation or inactivity. User Name: {{userName}} - Reason: {{reason}} - Timestamp: {{timestamp}} Please contact support if you believe this is an error. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.USER_SUSPENSION, "User Suspended In-App Template", "Account Suspended", "User {{userName}}'s account has been suspended due to {{reason}}."),

                // USER_SUSPENSION Templates (admin-side roles) - reuses USER_SUSPENDED admin copy.
                // Email only: the in-app message and the SMS text are already third-person and
                // user-addressed respectively in both types, so there is no admin copy to override with.
                createEmailTemplate(NotificationType.USER_SUSPENSION, NotificationRecipientRole.CLIENT_ADMIN, "User Suspension Client Admin Email Template", "User Account Suspended - {{companyName}}", USER_SUSPENDED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user account has been suspended due to a policy violation or inactivity. User Name: {{userName}} - Reason: {{reason}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createEmailTemplate(NotificationType.USER_SUSPENSION, NotificationRecipientRole.MSP, "User Suspension MSP Email Template", "User Account Suspended - {{companyName}}", USER_SUSPENDED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user account has been suspended due to a policy violation or inactivity. User Name: {{userName}} - Reason: {{reason}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createEmailTemplate(NotificationType.USER_SUSPENSION, NotificationRecipientRole.ASPIRE_ADMIN, "User Suspension Aspire Admin Email Template", "User Account Suspended - {{companyName}}", USER_SUSPENDED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user account has been suspended due to a policy violation or inactivity. User Name: {{userName}} - Reason: {{reason}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),

                // USER_ACTIVATED Templates
                createEmailTemplate(NotificationType.USER_ACTIVATED, "User Reactivated Email Template", "User Account Reactivated - {{userName}}", USER_ACTIVATED_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, We're pleased to inform you that your account has been successfully reactivated. You now have full access to your account and all platform features. User Name: {{userName}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.USER_ACTIVATED, "User Reactivated In-App Template", "Account Reactivated", "User {{userName}}'s account has been reactivated."),

                // USER_PASSWORD_CHANGE Templates (User)
                createEmailTemplate(NotificationType.USER_PASSWORD_CHANGE, "User Password Change Email Template", "Password Changed Successfully - {{companyName}}", USER_PASSWORD_CHANGE_USER_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Your password has been successfully changed on {{timestamp}}. If you didn't make this change, please contact support immediately. Best regards, The {{companyName}} Team"),
                createInAppTemplate(NotificationType.USER_PASSWORD_CHANGE, "User Password Change In-App Template", "Password Changed", "Your password was successfully changed at {{timestamp}}."),

                // USER_PASSWORD_CHANGE Templates (role overrides) - reuses USER_PASSWORD_CHANGE_ADMIN copy
                createEmailTemplate(NotificationType.USER_PASSWORD_CHANGE, NotificationRecipientRole.CLIENT_ADMIN, "User Password Change Client Admin Email Template", "User Password Changed - {{companyName}}", USER_PASSWORD_CHANGE_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has changed their password. User Name: {{userName}} Email: {{userEmail}} User Type: {{userType}} Changed At: {{timestamp}} Best regards, {{companyName}} Security Team"),
                createInAppTemplate(NotificationType.USER_PASSWORD_CHANGE, NotificationRecipientRole.CLIENT_ADMIN, "User Password Change Client Admin In-App Template", "User Password Changed", "User {{userName}} changed their password at {{timestamp}}."),
                createEmailTemplate(NotificationType.USER_PASSWORD_CHANGE, NotificationRecipientRole.MSP, "User Password Change MSP Email Template", "User Password Changed - {{companyName}}", USER_PASSWORD_CHANGE_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has changed their password. User Name: {{userName}} Email: {{userEmail}} User Type: {{userType}} Changed At: {{timestamp}} Best regards, {{companyName}} Security Team"),
                createInAppTemplate(NotificationType.USER_PASSWORD_CHANGE, NotificationRecipientRole.MSP, "User Password Change MSP In-App Template", "User Password Changed", "User {{userName}} changed their password at {{timestamp}}."),
                createEmailTemplate(NotificationType.USER_PASSWORD_CHANGE, NotificationRecipientRole.ASPIRE_ADMIN, "User Password Change Aspire Admin Email Template", "User Password Changed - {{companyName}}", USER_PASSWORD_CHANGE_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has changed their password. User Name: {{userName}} Email: {{userEmail}} User Type: {{userType}} Changed At: {{timestamp}} Best regards, {{companyName}} Security Team"),
                createInAppTemplate(NotificationType.USER_PASSWORD_CHANGE, NotificationRecipientRole.ASPIRE_ADMIN, "User Password Change Aspire Admin In-App Template", "User Password Changed", "User {{userName}} changed their password at {{timestamp}}."),

                // USER_PASSWORD_CHANGE_ADMIN Templates (Admin)
                createEmailTemplate(NotificationType.USER_PASSWORD_CHANGE_ADMIN, "User Password Change Admin Email Template", "User Password Changed - {{companyName}}", USER_PASSWORD_CHANGE_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has changed their password. User Name: {{userName}} Email: {{userEmail}} User Type: {{userType}} Changed At: {{timestamp}} Best regards, {{companyName}} Security Team"),
                createInAppTemplate(NotificationType.USER_PASSWORD_CHANGE_ADMIN, "User Password Change Admin In-App Template", "User Password Changed", "User {{userName}} changed their password at {{timestamp}}."),

                // NEW_COURSE_CREATED Templates
                createEmailTemplate(NotificationType.NEW_COURSE_CREATED, "New Course Created Email Template", "New Course Created - {{companyName}}", NEW_COURSE_CREATED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A new training course has been successfully created and is available in the system. - Course Title: {{courseTitle}} - Course Description: {{courseDescription}} - Access Permissions: {{permissions}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.NEW_COURSE_CREATED, "New Course Created In-App Template", "New Course Created", "New course '{{courseTitle}}' has been created and is now available."),

                // COURSE_UPDATED Templates
                createEmailTemplate(NotificationType.COURSE_UPDATED, "Course Updated Email Template", "Course Updated - {{companyName}}", COURSE_UPDATED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, An existing course has been updated in the system. - Course Title: {{courseTitle}} - Updated Fields: {{updatedFields}} - Update Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_UPDATED, "Course Updated In-App Template", "Course Updated", "Course '{{courseTitle}}' has been updated."),

                // COURSE_ASSIGNED Templates
                createEmailTemplate(NotificationType.COURSE_ASSIGNED, "Course Assigned Email Template", "Course Assigned - {{companyName}}", COURSE_ASSIGNED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. - User Name: {{userName}} - Course Title: {{courseTitle}} - Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_ASSIGNED, "Course Assigned In-App Template", "Course Assigned", "Course '{{courseTitle}}' has been assigned to {{userName}}."),

                // COURSE_COMPLETION Templates
                createEmailTemplate(NotificationType.COURSE_COMPLETION, "Course Completion Email Template", "Course Completed - {{companyName}}", COURSE_COMPLETION_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has completed a course. - User Name: {{userName}} - Course Title: {{courseTitle}} - Completion Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_COMPLETION, "Course Completion In-App Template", "Course Completed", "{{userName}} has completed the course '{{courseTitle}}'."),

                // COURSE_COMPLETION_USER Templates
                createEmailTemplate(NotificationType.COURSE_COMPLETION_USER, "Course Completion User Email Template", "Course Completed – {{courseTitle}}", COURSE_COMPLETION_USER_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Congratulations! You have successfully completed the training course \"{{courseTitle}}\" on {{completionDate}}. Thank you for completing your assigned training. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_COMPLETION_USER, "Course Completion User In-App Template", "Course Completed", "Congratulations! You have completed the course '{{courseTitle}}'."),

                // COURSE_COMPLETION_USER Templates (role overrides) - reuses COURSE_COMPLETION admin copy.
                // No MSP rows: COURSE_COMPLETION_USER is in MSP_DISABLED_TYPES.
                createEmailTemplate(NotificationType.COURSE_COMPLETION_USER, NotificationRecipientRole.CLIENT_ADMIN, "Course Completion Client Admin Email Template", "Course Completed - {{companyName}}", COURSE_COMPLETION_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has completed a course. - User Name: {{userName}} - Course Title: {{courseTitle}} - Completion Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_COMPLETION_USER, NotificationRecipientRole.CLIENT_ADMIN, "Course Completion Client Admin In-App Template", "Course Completed", "{{userName}} has completed the course '{{courseTitle}}'."),
                createEmailTemplate(NotificationType.COURSE_COMPLETION_USER, NotificationRecipientRole.ASPIRE_ADMIN, "Course Completion Aspire Admin Email Template", "Course Completed - {{companyName}}", COURSE_COMPLETION_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user has completed a course. - User Name: {{userName}} - Course Title: {{courseTitle}} - Completion Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_COMPLETION_USER, NotificationRecipientRole.ASPIRE_ADMIN, "Course Completion Aspire Admin In-App Template", "Course Completed", "{{userName}} has completed the course '{{courseTitle}}'."),

                // NEW_PACKAGE_CREATED Templates
                createEmailTemplate(NotificationType.NEW_PACKAGE_CREATED, "New Package Created Email Template", "New Package Created - {{companyName}}", NEW_PACKAGE_CREATED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A new service/product package has been created. - Package Name: {{packageName}} - Package Contents: {{contents}} - Pricing: {{pricing}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.NEW_PACKAGE_CREATED, "New Package Created In-App Template", "New Package Created", "New package '{{packageName}}' has been created."),

                // PACKAGE_ASSIGNED Templates (Admin)
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED, "Package Assigned Admin Email Template", "Course Assignment Confirmation - {{companyName}}", PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. User Name: {{userName}} Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED, "Package Assigned Admin In-App Template", "Course Assignment", "Course '{{packageName}}' has been assigned to {{userName}}."),

                // CLIENT_ADMIN_PACKAGE_ASSIGNED Templates
                createEmailTemplate(NotificationType.CLIENT_ADMIN_PACKAGE_ASSIGNED, "Client Admin Package Assigned Email Template", "Packages Assigned to Your Account - {{companyName}}", CLIENT_ADMIN_PACKAGE_ASSIGNED_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, New package assignments have been made to your organization account on {{assignmentDate}}. {{packageDetails}} Login: {{loginUrl}}"),
                createInAppTemplate(NotificationType.CLIENT_ADMIN_PACKAGE_ASSIGNED, "Client Admin Package Assigned In-App Template", "New Packages Assigned", "New package assignments have been made to your account on {{assignmentDate}}."),

                // PACKAGE_ASSIGNED_USER Templates (User)
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_USER, "Package Assigned User Email Template", "Course Assignment Confirmation - {{packageName}}", PACKAGE_ASSIGNED_USER_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Great news! A new course has been assigned to you. Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} You can now access your courses and start learning! Best regards, The {{companyName}} Team"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_USER, "Package Assigned User In-App Template", "New Course Assigned", "A new course '{{packageName}}' has been assigned to you!"),

                // PACKAGE_ASSIGNED_USER Templates (role overrides) - reuses PACKAGE_ASSIGNED admin copy
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_USER, NotificationRecipientRole.CLIENT_ADMIN, "Package Assigned Client Admin Email Template", "Course Assignment Confirmation - {{companyName}}", PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. User Name: {{userName}} Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_USER, NotificationRecipientRole.CLIENT_ADMIN, "Package Assigned Client Admin In-App Template", "Course Assignment", "Course '{{packageName}}' has been assigned to {{userName}}."),
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_USER, NotificationRecipientRole.MSP, "Package Assigned MSP Email Template", "Course Assignment Confirmation - {{companyName}}", PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. User Name: {{userName}} Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_USER, NotificationRecipientRole.MSP, "Package Assigned MSP In-App Template", "Course Assignment", "Course '{{packageName}}' has been assigned to {{userName}}."),
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_USER, NotificationRecipientRole.ASPIRE_ADMIN, "Package Assigned Aspire Admin Email Template", "Course Assignment Confirmation - {{companyName}}", PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. User Name: {{userName}} Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_USER, NotificationRecipientRole.ASPIRE_ADMIN, "Package Assigned Aspire Admin In-App Template", "Course Assignment", "Course '{{packageName}}' has been assigned to {{userName}}."),

                // PACKAGE_ASSIGNED_AND_USER_CREDENTIAL Templates (User)
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, "Package Assigned User Email Template", "Course Assignment Confirmation - {{packageName}}", PACKAGE_ASSIGNED_AND_USER_CREDENTIAL_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Great news! A new course has been assigned to you. Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} You can now access your courses and start learning! Best regards, The {{companyName}} Team"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, "Package Assigned User In-App Template", "New Course Assigned", "A new course '{{packageName}}' has been assigned to you!"),

                // PACKAGE_ASSIGNED_AND_USER_CREDENTIAL Templates (role overrides) - reuses PACKAGE_ASSIGNED admin copy.
                // No MSP rows: PACKAGE_ASSIGNED_AND_USER_CREDENTIAL is in MSP_DISABLED_TYPES.
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationRecipientRole.CLIENT_ADMIN, "Package Assigned Client Admin Email Template", "Course Assignment Confirmation - {{companyName}}", PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. User Name: {{userName}} Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationRecipientRole.CLIENT_ADMIN, "Package Assigned Client Admin In-App Template", "Course Assignment", "Course '{{packageName}}' has been assigned to {{userName}}."),
                createEmailTemplate(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationRecipientRole.ASPIRE_ADMIN, "Package Assigned Aspire Admin Email Template", "Course Assignment Confirmation - {{companyName}}", PACKAGE_ASSIGNED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A course has been assigned to a user. User Name: {{userName}} Course Name: {{packageName}} Course Details: {{packageDetails}} Assignment Date: {{assignmentDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, NotificationRecipientRole.ASPIRE_ADMIN, "Package Assigned Aspire Admin In-App Template", "Course Assignment", "Course '{{packageName}}' has been assigned to {{userName}}."),

                // PACKAGE_EXPIRY Templates
                createEmailTemplate(NotificationType.PACKAGE_EXPIRY, "Package Expiry Email Template", "Package Expiry Alert - {{companyName}}", PACKAGE_EXPIRY_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A user's package is nearing its expiration date. - User Name: {{userName}} - Package Name: {{packageName}} - Expiration Date: {{expirationDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PACKAGE_EXPIRY, "Package Expiry In-App Template", "Package Expiring Soon", "Package '{{packageName}}' for {{userName}} is expiring on {{expirationDate}}."),

                // CAMPAIGN_ACTIVITY Templates
                createEmailTemplate(NotificationType.CAMPAIGN_ACTIVITY, "Campaign Activity Email Template", "Campaign Activity Alert - {{companyName}}", CAMPAIGN_ACTIVITY_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, Significant campaign activity has occurred. - Campaign Type: {{campaignType}} - User Engagement: {{userEngagement}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.CAMPAIGN_ACTIVITY, "Campaign Activity In-App Template", "Campaign Activity", "Significant activity in campaign '{{campaignType}}'."),

                // PAYMENT_SUCCESS Templates
                createEmailTemplate(NotificationType.PAYMENT_SUCCESS, "Payment Success Email Template", "Payment Processed - {{companyName}}", PAYMENT_SUCCESS_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A payment has been successfully processed. - User Name: {{userName}} - Payment Amount: ${{amount}} - Payment Method: {{method}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PAYMENT_SUCCESS, "Payment Success In-App Template", "Payment Successful", "Payment of ${{amount}} from {{userName}} has been processed successfully."),

                // PAYMENT_FAILURE Templates
                createEmailTemplate(NotificationType.PAYMENT_FAILURE, "Payment Failure Email Template", "Payment Failed - {{companyName}}", PAYMENT_FAILURE_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A payment has failed due to an issue. - User Name: {{userName}} - Failure Reason: {{failureReason}} - Payment Amount: ${{amount}} - Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PAYMENT_FAILURE, "Payment Failure In-App Template", "Payment Failed", "Payment of ${{amount}} from {{userName}} failed: {{failureReason}}."),

                // PENDING_PAYMENT Templates
                createEmailTemplate(NotificationType.PENDING_PAYMENT, "Pending Payment Email Template", "Pending Payment Alert - {{companyName}}", PENDING_PAYMENT_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A payment is pending or delayed. - User Name: {{userName}} - Payment Amount: ${{amount}} - Due Date: {{dueDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.PENDING_PAYMENT, "Pending Payment In-App Template", "Payment Pending", "Payment of ${{amount}} from {{userName}} is pending. Due: {{dueDate}}."),

                // REFUND_REQUESTED Templates
                createEmailTemplate(NotificationType.REFUND_REQUESTED, "Refund Request Email Template", "Refund Request - {{companyName}}", REFUND_REQUEST_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A refund request has been submitted by a user. - User Name: {{userName}} - Refund Reason: {{refundReason}} - Refund Amount: ${{amount}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.REFUND_REQUESTED, "Refund Request In-App Template", "Refund Requested", "{{userName}} has requested a refund of {{amount}}."),

                // NEW_POLICY_CREATED Templates
                createEmailTemplate(NotificationType.NEW_POLICY_CREATED, "New Policy Created Email Template", "New Policy Created - {{companyName}}", NEW_POLICY_CREATED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A new policy has been successfully created or approved. - Policy Title: {{policyTitle}} - Description: {{policyDescription}} - Approval Status: {{approvalStatus}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.NEW_POLICY_CREATED, "New Policy Created In-App Template", "New Policy Created", "New policy '{{policyTitle}}' has been created."),

                // POLICY_UPDATED Templates
                createEmailTemplate(NotificationType.POLICY_UPDATED, "Policy Updated Email Template", "Policy Updated - {{companyName}}", POLICY_UPDATED_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, An existing policy has been updated. - Policy Title: {{policyTitle}} - Updated Fields: {{updatedFields}} - Update Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.POLICY_UPDATED, "Policy Updated In-App Template", "Policy Updated", "Policy '{{policyTitle}}' has been updated."),

                // POLICY_COMPLIANCE_REMINDER Templates
                createEmailTemplate(NotificationType.POLICY_COMPLIANCE_REMINDER, "Policy Compliance Reminder Email Template", "Policy Compliance Reminder - {{companyName}}", POLICY_COMPLIANCE_REMINDER_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, This is a reminder to review or update the following policy for compliance. - Policy Title: {{policyTitle}} - Reminder Date: {{reminderDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.POLICY_COMPLIANCE_REMINDER, "Policy Compliance Reminder In-App Template", "Compliance Reminder", "Policy '{{policyTitle}}' needs compliance review."),

                // CERTIFICATE_ISSUED Templates
                createEmailTemplate(NotificationType.CERTIFICATE_ISSUED, "Certificate Issued User Email Template", "🎓 Your Certificate Has Been Issued – {{courseTitle}}", CERTIFICATE_ISSUED_USER_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Congratulations! 🎉 We're pleased to inform you that your certificate for \"{{courseTitle}}\" has been successfully issued. Course Title: {{courseTitle}} Issue Date: {{issueDate}} You can now download or view your certificate from your {{companyName}} account. Thank you for your dedication and effort — we're proud to be part of your learning journey! Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.CERTIFICATE_ISSUED, "Certificate Issued In-App Template", "Certificate Issued", "Certificate for '{{courseTitle}}' has been issued to {{userName}}."),

                // CERTIFICATE_ISSUED_ADMIN Templates
                createEmailTemplate(NotificationType.CERTIFICATE_ISSUED_ADMIN, "Certificate Issued Admin Email Template", "Certificate Issued - {{companyName}}", CERTIFICATE_ISSUED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A certificate has been issued to a user. User Name: {{userName}} Course Title: {{courseTitle}} Issue Date: {{issueDate}} Best regards, {{companyName}}"),

                // CERTIFICATE_ISSUED Templates (role overrides) - reuses CERTIFICATE_ISSUED_ADMIN copy.
                // Email only: the base in-app message is already third-person, so every role inherits it.
                createEmailTemplate(NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.CLIENT_ADMIN, "Certificate Issued Client Admin Email Template", "Certificate Issued - {{companyName}}", CERTIFICATE_ISSUED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A certificate has been issued to a user. User Name: {{userName}} Course Title: {{courseTitle}} Issue Date: {{issueDate}} Best regards, {{companyName}}"),
                createEmailTemplate(NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.MSP, "Certificate Issued MSP Email Template", "Certificate Issued - {{companyName}}", CERTIFICATE_ISSUED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A certificate has been issued to a user. User Name: {{userName}} Course Title: {{courseTitle}} Issue Date: {{issueDate}} Best regards, {{companyName}}"),
                createEmailTemplate(NotificationType.CERTIFICATE_ISSUED, NotificationRecipientRole.ASPIRE_ADMIN, "Certificate Issued Aspire Admin Email Template", "Certificate Issued - {{companyName}}", CERTIFICATE_ISSUED_ADMIN_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A certificate has been issued to a user. User Name: {{userName}} Course Title: {{courseTitle}} Issue Date: {{issueDate}} Best regards, {{companyName}}"),

                // LEADERBOARD_UPDATE Templates
                createEmailTemplate(NotificationType.LEADERBOARD_UPDATE, "Leaderboard Update Email Template", "Leaderboard Updated - {{companyName}}", LEADERBOARD_UPDATE_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, The leaderboard has been updated with new top performers. - Top Performers: {{topPerformers}} - Update Timestamp: {{timestamp}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.LEADERBOARD_UPDATE, "Leaderboard Update In-App Template", "Leaderboard Updated", "Leaderboard has been updated with new top performers."),

                // CERTIFICATE_EXPIRY Templates
                createEmailTemplate(NotificationType.CERTIFICATE_EXPIRY, "Certificate Expiry Email Template", "Certificate Expiry Alert - {{companyName}}", CERTIFICATE_EXPIRY_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A certificate is about to expire or needs renewal. - User Name: {{userName}} - Certificate Title: {{certificateTitle}} - Expiration Date: {{expirationDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.CERTIFICATE_EXPIRY, "Certificate Expiry In-App Template", "Certificate Expiring", "Certificate '{{certificateTitle}}' for {{userName}} is expiring on {{expirationDate}}."),

                // CERTIFICATE_EXPIRING Templates
                createEmailTemplate(NotificationType.CERTIFICATE_EXPIRING, "Certificate Expiring Email Template", "Certificate Expiring Reminder - {{companyName}}", CERTIFICATE_EXPIRING_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, This is a friendly reminder that several of your team members' Security Awareness Training Certificates will be expiring on {{expirationDate}}. As the Client Admin, we kindly ask for your support in ensuring that your team members complete the necessary actions to renew or recertify their training before the expiration date. Next Steps: Log in to your Aspire Tech Portal/Training Portal: {{portalURL}}, Review Team's Training Status: Go to the Certifications section to view your users' current certificate statuses, Encourage Action: Please remind your team members to complete any required training or recertification steps before their certificates expire. Please open the attached CSV/Excel file for a detailed list of users with expiring certificates. The file contains all the necessary information and actions for renewal. If you need any assistance or have questions about the renewal process, please feel free to contact our support team at {{supportEmail}}. Thank you for your continued partnership in maintaining a secure and compliant environment! Best regards, The Aspire Tech Team"),
                createInAppTemplate(NotificationType.CERTIFICATE_EXPIRING, "Certificate Expiring In-App Template", "Certificates Expiring Soon", "Several team members' certificates are expiring on {{expirationDate}}. Please review the attached Excel file for details."),

                // SYSTEM_HEALTH_ALERTS Templates
                createEmailTemplate(NotificationType.SYSTEM_HEALTH_ALERTS, "System Downtime Email Template", "System Downtime Alert - {{companyName}}", SYSTEM_DOWNTIME_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, Please be advised that the system is currently experiencing downtime. Downtime Reason: {{reason}} - Start Time: {{startTime}} - End Time: {{endTime}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.SYSTEM_HEALTH_ALERTS, "System Downtime In-App Template", "System Downtime", "System is down for maintenance: {{reason}}."),

                // SECURITY_ALERTS Templates (Used for OTP verification codes)
                createEmailTemplate(NotificationType.SECURITY_ALERTS, "OTP Verification Code Email Template", "Verification Code - {{companyName}}", SECURITY_ALERT_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, Your verification code is {{otp}}. This code will expire in {{expiryMinutes}} minutes. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.SECURITY_ALERTS, "Security Alert In-App Template", "Security Alert", "Security alert: {{threatType}} affecting {{userName}}."),

                // UPDATES_PATCHES Templates
                createEmailTemplate(NotificationType.UPDATES_PATCHES, "Updates Patches Email Template", "System Updates Available - {{companyName}}", UPDATES_PATCHES_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, New updates or patches are available for the portal. - Patch/Update Version: {{version}} - Release Date: {{releaseDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.UPDATES_PATCHES, "Updates Patches In-App Template", "Updates Available", "New updates ({{version}}) are available for the portal."),

                // FEATURE_UPDATE_CHANGE Templates
                createEmailTemplate(NotificationType.FEATURE_UPDATE_CHANGE, "Feature Update Email Template", "Feature Update - {{companyName}}", FEATURE_UPDATE_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A new feature has been released or an existing feature has been updated. - Feature Name: {{featureName}} - Change Details: {{changeDetails}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.FEATURE_UPDATE_CHANGE, "Feature Update In-App Template", "Feature Updated", "Feature '{{featureName}}' has been updated."),

                // DOMAIN_VERIFICATION Templates
                createEmailTemplate(NotificationType.DOMAIN_VERIFICATION, "Domain Verification Email Template", "Domain Verification Code - {{domainName}}", DOMAIN_VERIFICATION_EMAIL_HTML_TEMPLATE, "Your domain verification code for {{domainName}} is {{verificationCode}}. This code will expire in {{expiryTime}}. If you did not request this verification, please ignore this email."),

                // REMINDER_PENDING_TASKS Templates
                createEmailTemplate(NotificationType.REMINDER_PENDING_TASKS, "Pending Tasks Reminder Email Template", "Overdue Tasks Reminder - {{companyName}}", PENDING_TASKS_REMINDER_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, You have overdue tasks that need your attention. - Task Type: {{taskType}} - Task Description: {{taskDescription}} - Due Date: {{dueDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.REMINDER_PENDING_TASKS, "Pending Tasks Reminder In-App Template", "Overdue Tasks", "You have overdue tasks: {{taskType}} due {{dueDate}}."),

                // SCHEDULED_MAINTENANCE Templates
                createEmailTemplate(NotificationType.SCHEDULED_MAINTENANCE, "Scheduled Maintenance Email Template", "Scheduled Maintenance - {{companyName}}", SCHEDULED_MAINTENANCE_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, System maintenance is scheduled as follows: - Maintenance Type: {{maintenanceType}} - Scheduled Date: {{scheduledDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.SCHEDULED_MAINTENANCE, "Scheduled Maintenance In-App Template", "Maintenance Scheduled", "System maintenance scheduled: {{maintenanceType}} on {{scheduledDate}}."),

                // HELP_DESK_TICKET_UPDATES Templates
                createEmailTemplate(NotificationType.HELP_DESK_TICKET_UPDATES, "Help Desk Ticket Email Template", "Help Desk Ticket Update - {{companyName}}", HELP_DESK_TICKET_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, A help desk ticket has been updated. Ticket ID: {{ticketId}} - Ticket Status: {{status}} - Resolution Date: {{resolutionDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.HELP_DESK_TICKET_UPDATES, "Help Desk Ticket In-App Template", "Ticket Updated", "Help desk ticket {{ticketId}} status: {{status}}."),

                // SUBSCRIPTION_RENEWAL_REMINDER Templates
                createEmailTemplate(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, "Subscription Renewal Email Template", "Subscription Renewal Reminder - {{companyName}}", SUBSCRIPTION_RENEWAL_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, Your portal's subscription or plan is about to expire. - Subscription Name: {{subscriptionName}} - Renewal Date: {{renewalDate}} Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, "Subscription Renewal In-App Template", "Subscription Expiring", "Subscription '{{subscriptionName}}' expires on {{renewalDate}}."),

                // BULK_USER_IMPORT_SUMMARY Templates
                createEmailTemplate(NotificationType.BULK_USER_IMPORT_SUMMARY, "Bulk User Import Summary Email Template", "Bulk User Import Summary - {{companyName}}", BULK_USER_IMPORT_SUMMARY_EMAIL_HTML_TEMPLATE, "Dear {{adminName}}, Your bulk user import has been completed.\n\nSuccessfully imported: {{totalImported}} users\nSkipped: {{totalSkipped}} users\nImport date: {{importDate}}\n\nSUCCESSFULLY IMPORTED USERS:\n{{importedUsersList}}\n\nSKIPPED USERS:\n{{skippedUsersList}}\n\nBest regards,\n{{companyName}}"),
                createInAppTemplate(NotificationType.BULK_USER_IMPORT_SUMMARY, "Bulk User Import Summary In-App Template", "Bulk Import Completed", "Bulk user import completed: {{totalImported}} imported, {{totalSkipped}} skipped."),

                // COURSE_NOT_STARTED_USER Templates
                createEmailTemplate(NotificationType.COURSE_NOT_STARTED_USER, "Course Not Started User Reminder Email Template", "Action Required: You Have Not Started the Assigned Course – {{subPackageName}}", COURSE_NOT_STARTED_USER_EMAIL_HTML_TEMPLATE, "Dear {{userName}}, This is a reminder that you have been assigned a course under the sub-package {{subPackageName}}. According to our records, you have not yet started the course. As part of the training process, it is important that you begin the course within the next 7 days to ensure timely completion. Please log in to your portal to begin the course as soon as possible. If you need assistance, feel free to contact {{supportEmail}}. Thank you for your attention. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_NOT_STARTED_USER, "Course Not Started User Reminder In-App Template", "Action Required: Course Not Started", "You have not started the assigned course: {{subPackageName}}. Please begin within 7 days."),

                // COURSE_NOT_STARTED_MANAGER Templates
                createEmailTemplate(NotificationType.COURSE_NOT_STARTED_MANAGER, "Course Not Started Manager Reminder Email Template", "Reminder: Employee {{userName}} Has Not Started the Assigned Course", COURSE_NOT_STARTED_MANAGER_EMAIL_HTML_TEMPLATE, "Dear {{managerName}}, This is a follow-up regarding the course assigned to your team member, {{userName}}, under the sub-package {{subPackageName}}. As of today, they have not started the course. Please reach out to {{userName}} to ensure they begin the course promptly and stay on track with their training. If you need any assistance or further information, feel free to contact {{supportEmail}}. Thank you for your attention to this matter. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_NOT_STARTED_MANAGER, "Course Not Started Manager Reminder In-App Template", "Employee Course Reminder", "Employee {{userName}} has not started the assigned course: {{subPackageName}}."),

                // MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL Templates
                createEmailTemplate(NotificationType.MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL, "Mandatory Training Assigned - Manager/HR/C-Level Email Template", "New Training Assigned: {{userName}} - {{courseName}}", MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL_EMAIL_HTML_TEMPLATE, "Dear {{emailReceiverName}}, We would like to inform you that {{userName}} from the {{departmentName}} has been assigned the mandatory security awareness training course. The completion of this course is required to ensure compliance with company policies and to enhance our overall security posture. Employee Details: Name: {{userName}}, Department: {{departmentName}}, Course/Sub-Package: {{courseName}}, Assignment Date: {{assignmentDate}}, Expiration Date: {{expirationDate}}. As part of their ongoing development, it is important that {{userName}} complete this training by the assigned expiration date. We encourage you to monitor their progress and provide any support as needed to ensure timely completion. Should you have any questions or need assistance with the training platform, feel free to reach out to our support team at {{supportEmail}}. Thank you for your attention and for supporting our commitment to security and compliance. Best regards, The ASAT Learning Platform"),
                createInAppTemplate(NotificationType.MANDATORY_TRAINING_ASSIGNED_MANAGER_HR_C_LEVEL, "Mandatory Training Assigned - Manager/HR/C-Level In-App Template", "Mandatory Training Assigned", "{{userName}} from {{departmentName}} has been assigned mandatory security awareness training: {{courseName}}. Expiration: {{expirationDate}}."),

                // COURSE_NOT_STARTED_C_LEVEL Templates
                createEmailTemplate(NotificationType.COURSE_NOT_STARTED_C_LEVEL, "Course Not Started C-Level Alert Email Template", "Urgent: Employee {{userName}} Has Not Started the Assigned Course", COURSE_NOT_STARTED_C_LEVEL_EMAIL_HTML_TEMPLATE, "Dear {{cLevelName}}, We wanted to inform you that your team member, {{userName}}, has still not started the course under the sub-package {{subPackageName}}, which was assigned {{daysSinceAssignment}} days ago. As this course is critical to their professional development, we recommend that immediate action be taken to address this issue. Please liaise with the respective Manager to ensure the course is completed on time. If you require further information or assistance, feel free to contact {{supportEmail}}. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_NOT_STARTED_C_LEVEL, "Course Not Started C-Level Alert In-App Template", "Urgent: Course Not Started Alert", "Employee {{userName}} has not started course {{subPackageName}} after {{daysSinceAssignment}} days."),

                // COURSE_EXPIRY_HR Templates
                createEmailTemplate(NotificationType.COURSE_EXPIRY_HR, "Course Expiry HR Final Reminder Email Template", "Final Reminder: Course Expiration for {{userName}}", COURSE_EXPIRY_HR_EMAIL_HTML_TEMPLATE, "Dear {{hrName}}, This is a final reminder regarding the course assigned to {{userName}} under the sub-package {{subPackageName}}. The course will expire in 2 days, and it has not yet been completed. Please reach out to the employee and their respective manager to ensure the course is completed before the expiration date. If you need any further assistance or have questions, please contact {{supportEmail}}. Thank you for your attention. Best regards, {{companyName}}"),
                createInAppTemplate(NotificationType.COURSE_EXPIRY_HR, "Course Expiry HR Final Reminder In-App Template", "Final Reminder: Course Expiring", "Course {{subPackageName}} for {{userName}} expires in 2 days and is not completed."),

                // SMS Templates — the 9 types (7 matrix rows + USER_SUSPENSION/SUBPACKAGE_EXPIRY_REMINDER
                // aliases) with SMS enabled in the MSP role scope matrix (see section 3a of the plan)
                createSmsTemplate(NotificationType.USER_SUSPENDED, "User Suspended SMS Template",
                        "Hi{{userName}}, your ASAT account has been suspended: {{reason}}. Contact support for assistance."),
                createSmsTemplate(NotificationType.USER_SUSPENSION, "User Suspended SMS Template",
                        "Hi{{userName}}, your ASAT account has been suspended: {{reason}}. Contact support for assistance."),
                createSmsTemplate(NotificationType.PACKAGE_EXPIRY, "Package Expiry SMS Template",
                        "Your ASAT package '{{packageName}}' for {{userName}} expires on {{expirationDate}}. Please renew soon."),
                createSmsTemplate(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, "SubPackage Expiry Reminder SMS Template",
                        "Your ASAT package '{{packageName}}' for {{userName}} expires on {{expirationDate}}. Please renew soon."),
                createSmsTemplate(NotificationType.PAYMENT_FAILURE, "Payment Failure SMS Template",
                        "Your ASAT payment of {{amount}} failed: {{failureReason}}. Please update your payment method."),
                createSmsTemplate(NotificationType.PENDING_PAYMENT, "Pending Payment SMS Template",
                        "Your ASAT payment of {{amount}} is pending, due {{dueDate}}. Please complete payment to avoid interruption."),
                createSmsTemplate(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, "Subscription Renewal Reminder SMS Template",
                        "Your ASAT subscription '{{subscriptionName}}' expires on {{renewalDate}}. Renew now to continue service."),
                createSmsTemplate(NotificationType.SYSTEM_HEALTH_ALERTS, "System Health Alert SMS Template",
                        "ASAT system alert: {{reason}}. Our team is working to resolve this."),
                createSmsTemplate(NotificationType.SECURITY_ALERTS, "Security Alert SMS Template",
                        "Hi{{userName}}, your ASAT verification code is {{otp}}. It expires in {{expiryMinutes}} minutes."),

                // Role-scoped SMS for SMS-matrix types (CLIENT_ADMIN / MSP / ASPIRE_ADMIN).
                // USER keeps the base SMS above via lookup fallback (27 rows).
                createSmsTemplate(NotificationType.USER_SUSPENDED, NotificationRecipientRole.CLIENT_ADMIN, "User Suspended Client Admin SMS Template", ADMIN_SMS_USER_SUSPENDED),
                createSmsTemplate(NotificationType.USER_SUSPENDED, NotificationRecipientRole.MSP, "User Suspended MSP SMS Template", ADMIN_SMS_USER_SUSPENDED),
                createSmsTemplate(NotificationType.USER_SUSPENDED, NotificationRecipientRole.ASPIRE_ADMIN, "User Suspended Aspire Admin SMS Template", ADMIN_SMS_USER_SUSPENDED),
                createSmsTemplate(NotificationType.USER_SUSPENSION, NotificationRecipientRole.CLIENT_ADMIN, "User Suspension Client Admin SMS Template", ADMIN_SMS_USER_SUSPENDED),
                createSmsTemplate(NotificationType.USER_SUSPENSION, NotificationRecipientRole.MSP, "User Suspension MSP SMS Template", ADMIN_SMS_USER_SUSPENDED),
                createSmsTemplate(NotificationType.USER_SUSPENSION, NotificationRecipientRole.ASPIRE_ADMIN, "User Suspension Aspire Admin SMS Template", ADMIN_SMS_USER_SUSPENDED),
                createSmsTemplate(NotificationType.PACKAGE_EXPIRY, NotificationRecipientRole.CLIENT_ADMIN, "Package Expiry Client Admin SMS Template", ADMIN_SMS_PACKAGE_EXPIRY),
                createSmsTemplate(NotificationType.PACKAGE_EXPIRY, NotificationRecipientRole.MSP, "Package Expiry MSP SMS Template", ADMIN_SMS_PACKAGE_EXPIRY),
                createSmsTemplate(NotificationType.PACKAGE_EXPIRY, NotificationRecipientRole.ASPIRE_ADMIN, "Package Expiry Aspire Admin SMS Template", ADMIN_SMS_PACKAGE_EXPIRY),
                createSmsTemplate(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, NotificationRecipientRole.CLIENT_ADMIN, "SubPackage Expiry Reminder Client Admin SMS Template", ADMIN_SMS_PACKAGE_EXPIRY),
                createSmsTemplate(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, NotificationRecipientRole.MSP, "SubPackage Expiry Reminder MSP SMS Template", ADMIN_SMS_PACKAGE_EXPIRY),
                createSmsTemplate(NotificationType.SUBPACKAGE_EXPIRY_REMINDER, NotificationRecipientRole.ASPIRE_ADMIN, "SubPackage Expiry Reminder Aspire Admin SMS Template", ADMIN_SMS_PACKAGE_EXPIRY),
                createSmsTemplate(NotificationType.PAYMENT_FAILURE, NotificationRecipientRole.CLIENT_ADMIN, "Payment Failure Client Admin SMS Template", ADMIN_SMS_PAYMENT_FAILURE),
                createSmsTemplate(NotificationType.PAYMENT_FAILURE, NotificationRecipientRole.MSP, "Payment Failure MSP SMS Template", ADMIN_SMS_PAYMENT_FAILURE),
                createSmsTemplate(NotificationType.PAYMENT_FAILURE, NotificationRecipientRole.ASPIRE_ADMIN, "Payment Failure Aspire Admin SMS Template", ADMIN_SMS_PAYMENT_FAILURE),
                createSmsTemplate(NotificationType.PENDING_PAYMENT, NotificationRecipientRole.CLIENT_ADMIN, "Pending Payment Client Admin SMS Template", ADMIN_SMS_PENDING_PAYMENT),
                createSmsTemplate(NotificationType.PENDING_PAYMENT, NotificationRecipientRole.MSP, "Pending Payment MSP SMS Template", ADMIN_SMS_PENDING_PAYMENT),
                createSmsTemplate(NotificationType.PENDING_PAYMENT, NotificationRecipientRole.ASPIRE_ADMIN, "Pending Payment Aspire Admin SMS Template", ADMIN_SMS_PENDING_PAYMENT),
                createSmsTemplate(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, NotificationRecipientRole.CLIENT_ADMIN, "Subscription Renewal Reminder Client Admin SMS Template", ADMIN_SMS_SUBSCRIPTION_RENEWAL),
                createSmsTemplate(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, NotificationRecipientRole.MSP, "Subscription Renewal Reminder MSP SMS Template", ADMIN_SMS_SUBSCRIPTION_RENEWAL),
                createSmsTemplate(NotificationType.SUBSCRIPTION_RENEWAL_REMINDER, NotificationRecipientRole.ASPIRE_ADMIN, "Subscription Renewal Reminder Aspire Admin SMS Template", ADMIN_SMS_SUBSCRIPTION_RENEWAL),
                createSmsTemplate(NotificationType.SYSTEM_HEALTH_ALERTS, NotificationRecipientRole.CLIENT_ADMIN, "System Health Alert Client Admin SMS Template", ADMIN_SMS_SYSTEM_HEALTH),
                createSmsTemplate(NotificationType.SYSTEM_HEALTH_ALERTS, NotificationRecipientRole.MSP, "System Health Alert MSP SMS Template", ADMIN_SMS_SYSTEM_HEALTH),
                createSmsTemplate(NotificationType.SYSTEM_HEALTH_ALERTS, NotificationRecipientRole.ASPIRE_ADMIN, "System Health Alert Aspire Admin SMS Template", ADMIN_SMS_SYSTEM_HEALTH),
                createSmsTemplate(NotificationType.SECURITY_ALERTS, NotificationRecipientRole.CLIENT_ADMIN, "Security Alert Client Admin SMS Template", ADMIN_SMS_SECURITY_ALERTS),
                createSmsTemplate(NotificationType.SECURITY_ALERTS, NotificationRecipientRole.MSP, "Security Alert MSP SMS Template", ADMIN_SMS_SECURITY_ALERTS),
                createSmsTemplate(NotificationType.SECURITY_ALERTS, NotificationRecipientRole.ASPIRE_ADMIN, "Security Alert Aspire Admin SMS Template", ADMIN_SMS_SECURITY_ALERTS)
        );

        NotificationTemplateSeedSync.Result syncResult =
                NotificationTemplateSeedSync.syncInsertOnly(templateRepository, templates);
        log.info("✅ Notification templates sync complete. Inserted: {}. Preserved (existing): {}. Total in database: {}.",
                syncResult.inserted(), syncResult.preserved(), templateRepository.count());
    }

    /**
     * Create email template
     */
    private NotificationTemplate createEmailTemplate(NotificationType type, String templateName,
                                                     String subjectTemplate, String htmlTemplate, String textTemplate) {
        return createEmailTemplate(type, null, templateName, subjectTemplate, htmlTemplate, textTemplate);
    }

    /**
     * Create a recipient-role-scoped email template. Used so a role such as
     * {@link NotificationRecipientRole#CLIENT_ADMIN} can receive different copy from the
     * default (role-agnostic) template for the same {@code type}. Falls back to the
     * role-agnostic template when no role-specific row exists (see
     * {@link com.aspire.asat.notification.service.NotificationTemplateService#getTemplate(NotificationType, NotificationChannel, NotificationRecipientRole)}).
     *
     * <p>Role-scoped greetings use {@code {{clientAdminName}}} / {@code {{mspName}}} /
     * {@code {{aspireAdminName}}} (auto-enriched from the registration recipient bundle).
     * Legacy base templates keep {@code {{adminName}}}.
     */
    private NotificationTemplate createEmailTemplate(NotificationType type, NotificationRecipientRole role, String templateName,
                                                     String subjectTemplate, String htmlTemplate, String textTemplate) {
        String greetingKey = roleGreetingPlaceholder(role);
        return NotificationTemplate.builder()
                .notificationType(type)
                .channel(NotificationChannel.EMAIL)
                .recipientRole(role)
                .templateName(templateName)
                .subjectTemplate(subjectTemplate)
                .htmlTemplate(remapAdminGreeting(htmlTemplate, greetingKey))
                .textTemplate(remapAdminGreeting(textTemplate, greetingKey))
                .isActive(true)
                // Only the role-agnostic base template is the "default" for a (type, channel) pair.
                .isDefault(role == null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Role-scoped email greeting placeholder. Base (null-role) templates keep {@code adminName}.
     */
    private static String roleGreetingPlaceholder(NotificationRecipientRole role) {
        if (role == null) {
            return "adminName";
        }
        return switch (role) {
            case CLIENT_ADMIN -> "clientAdminName";
            case MSP -> "mspName";
            case ASPIRE_ADMIN -> "aspireAdminName";
            case USER -> "userName";
        };
    }

    private static String remapAdminGreeting(String content, String greetingKey) {
        if (content == null || "adminName".equals(greetingKey)) {
            return content;
        }
        return content.replace("{{adminName}}", "{{" + greetingKey + "}}");
    }

    /**
     * Create in-app template
     */
    private NotificationTemplate createInAppTemplate(NotificationType type, String templateName,
                                                     String titleTemplate, String messageTemplate) {
        return createInAppTemplate(type, null, templateName, titleTemplate, messageTemplate);
    }

    /**
     * Create a recipient-role-scoped in-app template. See {@link #createEmailTemplate(NotificationType, NotificationRecipientRole, String, String, String, String)}.
     */
    private NotificationTemplate createInAppTemplate(NotificationType type, NotificationRecipientRole role, String templateName,
                                                     String titleTemplate, String messageTemplate) {
        return NotificationTemplate.builder()
                .notificationType(type)
                .channel(NotificationChannel.IN_APP)
                .recipientRole(role)
                .templateName(templateName)
                .titleTemplate(titleTemplate)
                .messageTemplate(messageTemplate)
                .isActive(true)
                // Only the role-agnostic base template is the "default" for a (type, channel) pair.
                .isDefault(role == null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create SMS template. SMS content is rendered from {@code textTemplate}
     * (see {@link com.aspire.asat.notification.service.NotificationTemplateService#generateTextContent}).
     */
    private NotificationTemplate createSmsTemplate(NotificationType type, String templateName, String textTemplate) {
        return createSmsTemplate(type, null, templateName, textTemplate);
    }

    /**
     * Create a recipient-role-scoped SMS template. Used so admin roles receive third-person
     * alert copy while USER keeps the role-agnostic base SMS. See
     * {@link com.aspire.asat.notification.service.NotificationTemplateService#getTemplate(NotificationType, NotificationChannel, NotificationRecipientRole)}.
     */
    private NotificationTemplate createSmsTemplate(NotificationType type, NotificationRecipientRole role,
                                                   String templateName, String textTemplate) {
        return NotificationTemplate.builder()
                .notificationType(type)
                .channel(NotificationChannel.SMS)
                .recipientRole(role)
                .templateName(templateName)
                .textTemplate(textTemplate)
                .isActive(true)
                // Only the role-agnostic base template is the "default" for a (type, channel) pair.
                .isDefault(role == null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
