package com.aspire.asat.common.annotation;

import com.aspire.asat.common.enums.ActivityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for activity logging.
 * When applied to a method, the CommonLoggingAspect will automatically log the activity.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    /**
     * The type of activity being performed
     */
    ActivityType activityType();

    /**
     * Description of the activity. Can use SpEL expressions to reference method parameters.
     * Example: "Created client admin: #{#requestDto.organizationName}"
     */
    String description() default "";

    /**
     * SpEL expression to extract the old value for update operations.
     * Example: "#{#clientAdminId}" or "#{#requestDto.organizationName}"
     */
    String oldValueExpression() default "";

    /**
     * SpEL expression to extract the new value for update operations.
     * Example: "#{#updateRequestDto.organizationName}"
     */
    String newValueExpression() default "";

    /**
     * SpEL expression to extract the countryId.
     * If not provided, countryId will be auto-derived from CurrentUserContext.
     */
    String countryIdExpression() default "";

    /**
     * SpEL expression to extract the mspId.
     * If not provided, mspId will be auto-derived from CurrentUserContext based on userType.
     */
    String mspIdExpression() default "";

    /**
     * SpEL expression to extract the clientAdminId.
     * If not provided, clientAdminId will be auto-derived from CurrentUserContext based on userType.
     */
    String clientAdminIdExpression() default "";

    /**
     * Whether to log on method entry (before execution)
     */
    boolean logOnEntry() default false;
}

