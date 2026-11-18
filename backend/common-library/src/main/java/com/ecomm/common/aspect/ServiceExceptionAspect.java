package com.ecomm.common.aspect;

import com.ecomm.common.exception.BaseDomainException;
import com.ecomm.common.exception.MeaningfulErrorTranslator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AspectJ interceptor for Service and Repository layers across all e-comm microservices.
 *
 * Capabilities:
 * 1. Intercepts method execution in all @Service and @Repository beans.
 * 2. Profiles execution duration and correlates with distributed tracing correlation IDs.
 * 3. Catches low-level database, SQL, JPA, and concurrency exceptions.
 * 4. Translates raw technical exceptions into clean, meaningful, actionable domain exceptions.
 * 5. Masks sensitive parameters in diagnostic logs.
 */
@Aspect
@Component
@Order(10)
public class ServiceExceptionAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExceptionAspect.class);
    private static final String MDC_CORRELATION_KEY = "correlationId";

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceBeanPointcut() {}

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryBeanPointcut() {}

    @Around("serviceBeanPointcut() || repositoryBeanPointcut()")
    public Object interceptAndTranslate(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String targetMethod = joinPoint.getSignature().toShortString();
        String correlationId = getCorrelationId();

        if (log.isDebugEnabled()) {
            log.debug("[{}] [AspectJ:Entry] Invoking {} with args: {}",
                    correlationId, targetMethod, sanitizeArguments(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            if (log.isDebugEnabled()) {
                log.debug("[{}] [AspectJ:Success] {} completed successfully in {}ms",
                        correlationId, targetMethod, duration);
            }
            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            Throwable translated = MeaningfulErrorTranslator.translate(throwable);

            if (translated instanceof BaseDomainException domainException) {
                log.warn("[{}] [AspectJ:ExceptionHandled] {} failed after {}ms: [{}] {}",
                        correlationId, targetMethod, duration, domainException.getErrorCode(), domainException.getMessage());
            } else {
                log.error("[{}] [AspectJ:ExceptionUnhandled] {} failed after {}ms with unexpected exception: {}",
                        correlationId, targetMethod, duration, throwable.getMessage(), throwable);
            }

            throw translated;
        }
    }

    private String getCorrelationId() {
        String id = MDC.get(MDC_CORRELATION_KEY);
        return (id != null && !id.isBlank()) ? id : "N/A";
    }

    private String sanitizeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                String str = arg.toString();
                // Simple regex masking for sensitive fields in case passwords or secrets appear
                str = str.replaceAll("(?i)(password|secret|token)=[^,\\)]+", "$1=***");
                sb.append(str);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
