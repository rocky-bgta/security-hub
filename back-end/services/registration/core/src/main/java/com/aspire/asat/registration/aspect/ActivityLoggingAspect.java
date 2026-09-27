package com.aspire.asat.registration.aspect;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.ActivityLog;
import com.aspire.asat.registration.service.ActivityLogService;
import com.aspire.asat.common.util.IpAddressUtil;
import com.aspire.asat.common.util.UserTypeInferenceUtil;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * AOP Aspect for automatic activity logging.
 * Intercepts methods annotated with @LogActivity and logs the activity to ActivityLog table.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final ActivityLogService activityLogService;
    private final UserCurrentContextService userCurrentContextService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around("@annotation(logActivity)")
    public Object logActivityMethod(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Extract method parameters for SpEL evaluation
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();
        
        // Create evaluation context for SpEL expressions
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        
        // Get user context and IP address
        CurrentUserContext userContext = null;
        String ipAddress = null;
        String userId = null;
        String email = null;
        String username = null;
        String fullName = null;
        UserType userType = null;
        
        try {
            userContext = userCurrentContextService.getCurrentUserContext();
            if (userContext != null) {
                userId = userContext.getUserId();
                email = userContext.getEmail();
                username = userContext.getUsername();
                fullName = userContext.getFullName();
                // Map userType from CurrentUserContext (infer from string or from context when null/legacy)
                String userTypeString = userContext.getUserType();
                if (userTypeString != null && !userTypeString.isBlank()) {
                    userType = UserTypeInferenceUtil.inferUserTypeFromString(userTypeString);
                    if (userType == null) {
                        log.warn("Unknown user type: {}, defaulting to SYSTEM_USER", userTypeString);
                        userType = UserType.SYSTEM_USER;
                    }
                } else {
                    // Infer from mspId/clientAdminId vs userId so ASPIRE_ADMIN and MSP get correct activity log
                    userType = UserTypeInferenceUtil.inferUserTypeFromContext(
                            userId, userContext.getMspId(), userContext.getClientAdminId());
                    if (userType == null) {
                        userType = UserType.SYSTEM_USER;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get user context: {}", e.getMessage());
            userType = UserType.SYSTEM_USER;
        }
        
        // Get IP address from request
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = IpAddressUtil.getIpAddress(request);
            }
        } catch (Exception e) {
            log.warn("Failed to get IP address: {}", e.getMessage());
        }
        
        // Evaluate description SpEL expression
        String description = evaluateSpELExpression(logActivity.description(), context, 
                method.getName() + " executed");
        
        // Evaluate oldValue and newValue expressions if provided
        String oldValue = null;
        String newValue = null;
        
        if (!logActivity.oldValueExpression().isEmpty()) {
            oldValue = evaluateSpELExpression(logActivity.oldValueExpression(), context, null);
        }
        
        if (!logActivity.newValueExpression().isEmpty()) {
            newValue = evaluateSpELExpression(logActivity.newValueExpression(), context, null);
        }
        
        // Derive countryId, aspireAdminId, mspId, clientAdminId
        String countryId = deriveField(logActivity.countryIdExpression(), context,
                userContext != null ? userContext.getCountryId() : null);
        String aspireAdminId = null;
        String mspId = deriveField(logActivity.mspIdExpression(), context,
                userContext != null ? userContext.getMspId() : null);
        String clientAdminId = null;

        // Auto-derive based on userType (preserve SpEL/context value when already set)
        if (userType != null) {
            switch (userType) {
                case ASPIRE_ADMIN:
                case SUPER_ADMIN:
                case SYSTEM_USER:
                    aspireAdminId = userId;
                    break;
                case MSP:
                    mspId = (mspId == null || mspId.isEmpty()) ? userId : mspId;
                    break;
                case CLIENT_ADMIN:
                    clientAdminId = (clientAdminId == null || clientAdminId.isEmpty()) ? userId : clientAdminId;
                    break;
                case USER:
                case CLIENT:
                    clientAdminId = userContext != null ? userContext.getClientAdminId() : null;
                    // mspId already set from deriveField(context)
                    break;
                default:
                    break;
            }
        }
        
        // Log on entry if requested
        if (logActivity.logOnEntry()) {
            logActivityEntry(logActivity.activityType(), userId, countryId, aspireAdminId, mspId, clientAdminId,
                    email, username, fullName, userType, description, oldValue, newValue, ipAddress);
        }
        
        // Execute the method
        Object result = null;
        ActivityStatus status = ActivityStatus.SUCCESS;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            status = ActivityStatus.FAILED;
            // Update description with error message
            if (description != null && !description.isEmpty()) {
                description = description + " - Failed: " + e.getMessage();
            } else {
                description = method.getName() + " failed: " + e.getMessage();
            }
            throw e;
        } finally {
            // Log the activity
            saveActivityLog(logActivity.activityType(), userId, countryId, aspireAdminId, mspId, clientAdminId,
                    email, username, fullName, userType, status, description, oldValue, newValue, ipAddress);
        }
    }
    
    /**
     * Log activity entry (before method execution)
     */
    private void logActivityEntry(ActivityType activityType, String userId, String countryId, String aspireAdminId,
                                  String mspId, String clientAdminId, String email, String username, String fullName,
                                  UserType userType, String description, String oldValue, String newValue, String ipAddress) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .userId(userId)
                    .countryId(countryId)
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .email(email)
                    .username(username)
                    .fullName(fullName)
                    .userType(userType)
                    .activityType(activityType)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .description("Starting: " + description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .createdAt(Instant.now())
                    .build();
            
            activityLogService.logActivity(activityLog);
        } catch (Exception e) {
            log.error("Failed to log activity entry: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log activity after method execution
     */
    private void saveActivityLog(ActivityType activityType, String userId, String countryId, String aspireAdminId,
                            String mspId, String clientAdminId, String email, String username, String fullName,
                            UserType userType, ActivityStatus status, String description, String oldValue, 
                            String newValue, String ipAddress) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .userId(userId)
                    .countryId(countryId)
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .email(email)
                    .username(username)
                    .fullName(fullName)
                    .userType(userType)
                    .activityType(activityType)
                    .activityStatus(status)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .createdAt(Instant.now())
                    .build();
            
            activityLogService.logActivity(activityLog);
            log.debug("Activity logged: {} - {} - {}", activityType, userId, status);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage(), e);
            // Don't throw exception to avoid disrupting the main flow
        }
    }

    /**
     * Derive a field value: SpEL expression takes priority, then fallback to context value.
     */
    private String deriveField(String spelExpression, EvaluationContext context, String contextValue) {
        if (spelExpression != null && !spelExpression.isEmpty()) {
            String evaluated = evaluateSpELExpression(spelExpression, context, null);
            if (evaluated != null && !evaluated.isEmpty()) {
                return evaluated;
            }
        }
        return contextValue;
    }
    
    /**
     * Evaluate SpEL expression
     */
    private String evaluateSpELExpression(String expression, EvaluationContext context, String defaultValue) {
        if (expression == null || expression.isEmpty()) {
            return defaultValue;
        }
        
        try {
            Expression expr;
            if (expression.contains("#{")) {
                expr = expressionParser.parseExpression(expression, new TemplateParserContext());
            } else {
                expr = expressionParser.parseExpression(expression);
            }
            Object value = expr.getValue(context);
            return value != null ? value.toString() : defaultValue;
        } catch (Exception e) {
            log.warn("Failed to evaluate SpEL expression '{}': {}", expression, e.getMessage());
            return defaultValue;
        }
    }
    
}

