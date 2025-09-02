package com.synapse.lazy_init_group_spring_boot_starter.aspect;

import com.synapse.lazy_init_group_api.annotation.LazyInitGroup;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Aspect
@Component
public class LazyInitLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LazyInitLoggingAspect.class);
    
    private static final ThreadLocal<Map<String, Boolean>> initializationStatus = 
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Pointcut("@within(com.synapse.lazy_init_group_api.annotation.LazyInitGroup) && execution(public * *.*(..))")
    public void lazyInitGroupMethods() {
    }

    @Around("lazyInitGroupMethods()")
    public Object logLazyInitialization(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        String className = targetClass.getSimpleName();
        
        Map<String, Boolean> statusMap = initializationStatus.get();
        
        if (!statusMap.getOrDefault(className, false)) {
            LazyInitGroup annotation = targetClass.getAnnotation(LazyInitGroup.class);
            String groupName = annotation != null ? annotation.value() : "unknown";
            
            logger.info("[LAZY-INIT] LazyInitGroup '{}' (클래스: {}) is being initialized by method call: {}...", 
                       groupName, className, joinPoint.getSignature().getName());
            
            statusMap.put(className, true);
        }
        
        return joinPoint.proceed();
    }
    
    public static void clearInitializationStatus() {
        Map<String, Boolean> statusMap = initializationStatus.get();
        if (statusMap != null && !statusMap.isEmpty()) {
            logger.debug("[LAZY-INIT] ThreadLocal 초기화 상태 정리: {} 개 항목 제거", statusMap.size());
            statusMap.clear();
        }
        initializationStatus.remove();
    }
    
    public static Map<String, Boolean> getInitializationStatus() {
        Map<String, Boolean> statusMap = initializationStatus.get();
        return statusMap != null ? Map.copyOf(statusMap) : Map.of();
    }
    
    public static boolean isInitialized(String className) {
        Map<String, Boolean> statusMap = initializationStatus.get();
        return statusMap != null && statusMap.getOrDefault(className, false);
    }
    
    public static int getInitializedClassCount() {
        Map<String, Boolean> statusMap = initializationStatus.get();
        return statusMap != null ? statusMap.size() : 0;
    }
}
