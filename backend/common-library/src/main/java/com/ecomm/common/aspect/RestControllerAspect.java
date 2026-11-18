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

/**
 * AspectJ interceptor for the REST Controller layer.
 * Tracks incoming web request execution latency and ensures meaningful error
 * logging and domain exception translation before reaching Spring's @RestControllerAdvice.
 */
@Aspect
@Component
@Order(5)
public class RestControllerAspect {

    private static final Logger log = LoggerFactory.getLogger(RestControllerAspect.class);
    private static final String MDC_CORRELATION_KEY = "correlationId";

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {}

    @Around("restControllerPointcut()")
    public Object interceptControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String controllerAction = joinPoint.getSignature().toShortString();
        String correlationId = getCorrelationId();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] [AspectJ:Controller] {} executed successfully in {}ms",
                    correlationId, controllerAction, duration);
            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            Throwable translated = MeaningfulErrorTranslator.translate(throwable);

            if (translated instanceof BaseDomainException domainException) {
                log.warn("[{}] [AspectJ:ControllerException] {} intercepted exception after {}ms: [{}] {}",
                        correlationId, controllerAction, duration, domainException.getErrorCode(), domainException.getMessage());
            } else {
                log.error("[{}] [AspectJ:ControllerUnhandled] {} failed after {}ms: {}",
                        correlationId, controllerAction, duration, throwable.getMessage(), throwable);
            }

            throw translated;
        }
    }

    private String getCorrelationId() {
        String id = MDC.get(MDC_CORRELATION_KEY);
        return (id != null && !id.isBlank()) ? id : "N/A";
    }
}
