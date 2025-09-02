package com.synapse.lazy_init_group_spring_boot_starter.test;

import com.synapse.lazy_init_group_spring_boot_starter.aspect.LazyInitLoggingAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class LazyInitThreadLocalCleanupListener implements TestExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(LazyInitThreadLocalCleanupListener.class);

    @Override
    public void beforeTestMethod(TestContext testContext) {
        logger.debug("[LAZY-INIT-CLEANUP] 테스트 메서드 시작 전 ThreadLocal 정리: {}.{}", 
                    testContext.getTestClass().getSimpleName(), 
                    testContext.getTestMethod().getName());
        
        LazyInitLoggingAspect.clearInitializationStatus();
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        logger.debug("[LAZY-INIT-CLEANUP] 테스트 메서드 완료 후 ThreadLocal 정리: {}.{}", 
                    testContext.getTestClass().getSimpleName(), 
                    testContext.getTestMethod().getName());
        
        LazyInitLoggingAspect.clearInitializationStatus();
    }

    @Override
    public void afterTestExecution(TestContext testContext) {
        logger.debug("[LAZY-INIT-CLEANUP] 테스트 실행 완료 후 ThreadLocal 정리: {}.{}", 
                    testContext.getTestClass().getSimpleName(), 
                    testContext.getTestMethod().getName());
        
        LazyInitLoggingAspect.clearInitializationStatus();
    }
}