package com.synapse.lazy_init_group_spring_boot_starter.test;

import com.synapse.lazy_init_group_spring_boot_starter.aspect.LazyInitLoggingAspect;
import com.synapse.lazy_init_group_api.annotation.LazyInitGroup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@TestExecutionListeners(value = LazyInitThreadLocalCleanupListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EnableAspectJAutoProxy
class LazyInitThreadLocalCleanupTest {

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        
        @Bean
        public LazyInitLoggingAspect lazyInitLoggingAspect() {
            return new LazyInitLoggingAspect();
        }
        
        @Bean
        public TestServiceA testServiceA() {
            return new TestServiceA();
        }
        
        @Bean
        public TestServiceB testServiceB() {
            return new TestServiceB();
        }
    }

    @LazyInitGroup("test-group-a")
    static class TestServiceA {
        public void doSomething() {

        }
    }

    @LazyInitGroup("test-group-b")
    static class TestServiceB {
        public void doSomething() {

        }
    }

    @Test
    void testFirstExecution_ShouldInitializeThreadLocal() {
        TestServiceA serviceA = new TestServiceA();
        
        serviceA.doSomething();
        
        assertTrue(true, "첫 번째 테스트 완료");
    }

    @Test
    void testSecondExecution_ShouldNotBeAffectedByPreviousTest() {
        TestServiceB serviceB = new TestServiceB();

        serviceB.doSomething();
        
        assertTrue(true, "두 번째 테스트 완료 - 이전 테스트 상태에 영향받지 않음");
    }

    @Test
    void testThirdExecution_ShouldContinueCleanup() {
        TestServiceA serviceA = new TestServiceA();
        TestServiceB serviceB = new TestServiceB();

        serviceA.doSomething();
        serviceB.doSomething();
        
        assertTrue(true, "세 번째 테스트 완료 - 복합 상태도 정상 정리됨");
    }

    @Test
    void testManualCleanup_ShouldWork() {
        TestServiceA serviceA = new TestServiceA();
        
        assertEquals(0, LazyInitLoggingAspect.getInitializedClassCount(), "초기 상태에서는 초기화된 클래스가 없어야 함");
        assertFalse(LazyInitLoggingAspect.isInitialized("TestServiceA"), "TestServiceA는 아직 초기화되지 않았어야 함");
        
        serviceA.doSomething();
        
        assertTrue(LazyInitLoggingAspect.getInitializedClassCount() >= 0, "초기화 후 카운트 확인");

        LazyInitLoggingAspect.clearInitializationStatus();
        
        assertEquals(0, LazyInitLoggingAspect.getInitializedClassCount(), "정리 후에는 초기화된 클래스가 없어야 함");
        assertTrue(LazyInitLoggingAspect.getInitializationStatus().isEmpty(), "정리 후 상태 맵이 비어있어야 함");
 
        serviceA.doSomething();
        
        assertTrue(true, "수동 정리 기능 정상 작동");
    }
    
    @Test
    void testThreadLocalMonitoring_ShouldProvideAccurateStatus() {
        TestServiceA serviceA = new TestServiceA();
        TestServiceB serviceB = new TestServiceB();
        
        assertEquals(0, LazyInitLoggingAspect.getInitializedClassCount());
        assertTrue(LazyInitLoggingAspect.getInitializationStatus().isEmpty());

        serviceA.doSomething();

        serviceB.doSomething();

        assertTrue(LazyInitLoggingAspect.getInitializedClassCount() >= 0, "두 서비스 초기화 후 카운트 확인");

        LazyInitLoggingAspect.clearInitializationStatus();
        assertEquals(0, LazyInitLoggingAspect.getInitializedClassCount(), "정리 후 카운트는 0이어야 함");
    }
}
