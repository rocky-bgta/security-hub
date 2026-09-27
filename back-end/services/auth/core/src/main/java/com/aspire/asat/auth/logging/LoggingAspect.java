package com.aspire.asat.auth.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logControllerActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // Skip Swagger/actuator
        String uri = request.getRequestURI();
        if (uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs") || uri.startsWith("/actuator")) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        String queryParams = request.getQueryString();

        log.info("➡️ [{}] {}?{} | IP={} | UA={} | ClassMethod={} | Args={} | Time={}",
                method,
                uri,
                queryParams != null ? queryParams : "",
                ip,
                userAgent,
                className + "." + methodName + "()",
                Arrays.toString(joinPoint.getArgs()),
                LocalDateTime.now()
        );

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("❌ Exception in {}.{}(): {}", className, methodName, ex.getMessage(), ex);
            throw ex;
        }

        long duration = System.currentTimeMillis() - start;

        log.info("✅ Completed {}.{}() in {} ms", className, methodName, duration);
        return result;
    }
}
