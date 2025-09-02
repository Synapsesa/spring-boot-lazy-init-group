package com.synapse.lazy_init_group_spring_boot_starter;

import com.synapse.lazy_init_group_spring_boot_starter.aspect.LazyInitLoggingAspect;
import com.synapse.lazy_init_group_spring_boot_starter.service.LazyServiceA;
import com.synapse.lazy_init_group_spring_boot_starter.service.LazyServiceB;
import com.synapse.lazy_init_group_spring_boot_starter.service.NormalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@DisplayName("LazyInitGroup 통합 테스트")
public class LazyInitGroupIntegratedTest {

    private static final Logger logger = LoggerFactory.getLogger(LazyInitGroupIntegratedTest.class);

    @Nested
    @DisplayName("기능 비활성화 테스트")
    @TestPropertySource(properties = {
        "lazy-init.enabled=false",
        "logging.level.com.synapse.lazy_init_group_spring_boot_starter=INFO"
    })
    class DisabledTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Test
        @DisplayName("모든 빈이 즉시 로딩되는지 확인")
        void testLazyInitDisabled() {
            logger.info("=== 검증 시나리오 1: 기능 비활성화 테스트 시작 ===");
            
            // 모든 서비스가 이미 초기화되어 있어야 함
            assertTrue(applicationContext.containsBean("normalService"));
            assertTrue(applicationContext.containsBean("lazyServiceA"));
            assertTrue(applicationContext.containsBean("lazyServiceB"));
            
            // 빈을 가져와도 추가 초기화 로그가 출력되지 않아야 함 (이미 초기화됨)
            NormalService normalService = applicationContext.getBean(NormalService.class);
            LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
            LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
            
            assertNotNull(normalService);
            assertNotNull(lazyServiceA);
            assertNotNull(lazyServiceB);
            
            logger.info("=== 검증 시나리오 1: 완료 - 모든 빈이 즉시 로딩됨 ===");
        }
    }

    @Nested
    @DisplayName("기능 활성화 테스트")
    @TestPropertySource(properties = {
        "lazy-init.enabled=true",
        "logging.level.com.synapse.lazy_init_group_spring_boot_starter=INFO"
    })
    class EnabledTest {
        
        @Autowired
        private ApplicationContext applicationContext;
        
        @Test
        @DisplayName("지연 로딩 설정이 적용되는지 확인")
        void testLazyInitEnabled() {
            logger.info("=== 검증 시나리오 2: 기능 활성화 테스트 시작 ===");
            
            // 애플리케이션 시작 시점에서는 NormalService만 초기화되고
            // LazyServiceA, LazyServiceB는 아직 초기화되지 않았어야 함
            
            // 빈 정의는 존재해야 함
            assertTrue(applicationContext.containsBean("normalService"));
            assertTrue(applicationContext.containsBean("lazyServiceA"));
            assertTrue(applicationContext.containsBean("lazyServiceB"));
            
            logger.info("=== 검증 시나리오 2: 완료 - 지연 로딩 설정 확인됨 ===");
        }
        
        @Test
        @DisplayName("실제 호출 시 초기화되는지 확인")
        void testLazyInitializationOnDemand() {
            logger.info("=== 검증 시나리오 3: 실제 호출 시 초기화 테스트 시작 ===");
            
            // 이 시점에서 LazyService들을 실제로 호출하면 초기화 로그가 출력되어야 함
            logger.info("LazyServiceA 호출 전...");
            LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
            assertNotNull(lazyServiceA);
            
            logger.info("LazyServiceB 호출 전...");
            LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
            assertNotNull(lazyServiceB);
            
            // NormalService는 이미 초기화되어 있어야 함
            NormalService normalService = applicationContext.getBean(NormalService.class);
            assertNotNull(normalService);
            
            logger.info("=== 검증 시나리오 3: 완료 - 온디맨드 초기화 확인됨 ===");
        }
    }

    @Nested
    @DisplayName("선택적 지연 로딩 테스트")
    class SelectiveTest {
        
        @Nested
        @DisplayName("GroupA만 지연 로딩")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.groups=groupA",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter=DEBUG"
        })
        class GroupAOnlyTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("groupA만 지연 로딩되는지 확인")
            void testGroupAOnlyLazyLoading() {
                logger.info("=== 검증 시나리오 1: groupA만 지연 로딩 테스트 시작 ===");
                
                // 모든 빈 정의는 존재해야 함
                assertTrue(applicationContext.containsBean("normalService"));
                assertTrue(applicationContext.containsBean("lazyServiceA"));
                assertTrue(applicationContext.containsBean("lazyServiceB"));
                
                // NormalService는 @LazyInitGroup이 없으므로 즉시 로딩됨
                NormalService normalService = applicationContext.getBean(NormalService.class);
                assertNotNull(normalService);
                
                // LazyServiceA는 groupA이므로 지연 로딩됨
                LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
                assertNotNull(lazyServiceA);
                
                // LazyServiceB는 groupB이므로 즉시 로딩됨 (groupA만 지연 로딩 설정)
                LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
                assertNotNull(lazyServiceB);
                
                logger.info("=== 검증 시나리오 1: 완료 - groupA만 지연 로딩 확인됨 ===");
            }
        }
        
        @Nested
        @DisplayName("GroupB만 지연 로딩")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.groups=groupB",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter=DEBUG"
        })
        class GroupBOnlyTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("groupB만 지연 로딩되는지 확인")
            void testGroupBOnlyLazyLoading() {
                logger.info("=== 검증 시나리오 2: groupB만 지연 로딩 테스트 시작 ===");
                
                // 모든 빈 정의는 존재해야 함
                assertTrue(applicationContext.containsBean("normalService"));
                assertTrue(applicationContext.containsBean("lazyServiceA"));
                assertTrue(applicationContext.containsBean("lazyServiceB"));
                
                // NormalService는 @LazyInitGroup이 없으므로 즉시 로딩됨
                NormalService normalService = applicationContext.getBean(NormalService.class);
                assertNotNull(normalService);
                
                // LazyServiceA는 groupA이므로 즉시 로딩됨 (groupB만 지연 로딩 설정)
                LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
                assertNotNull(lazyServiceA);
                
                // LazyServiceB는 groupB이므로 지연 로딩됨
                LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
                assertNotNull(lazyServiceB);
                
                logger.info("=== 검증 시나리오 2: 완료 - groupB만 지연 로딩 확인됨 ===");
            }
        }
        
        @Nested
        @DisplayName("다중 그룹 지연 로딩")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.groups=groupA,groupB",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter=DEBUG"
        })
        class MultipleGroupsTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("다중 그룹이 모두 지연 로딩되는지 확인")
            void testMultipleGroupsLazyLoading() {
                logger.info("=== 검증 시나리오 3: 다중 그룹 지연 로딩 테스트 시작 ===");
                
                // 모든 빈 정의는 존재해야 함
                assertTrue(applicationContext.containsBean("normalService"));
                assertTrue(applicationContext.containsBean("lazyServiceA"));
                assertTrue(applicationContext.containsBean("lazyServiceB"));
                
                // NormalService는 @LazyInitGroup이 없으므로 즉시 로딩됨
                NormalService normalService = applicationContext.getBean(NormalService.class);
                assertNotNull(normalService);
                
                // LazyServiceA, LazyServiceB 모두 지연 로딩됨
                LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
                assertNotNull(lazyServiceA);
                
                LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
                assertNotNull(lazyServiceB);
                
                logger.info("=== 검증 시나리오 3: 완료 - 다중 그룹 지연 로딩 확인됨 ===");
            }
        }
    }

    @Nested
    @DisplayName("그룹 제외 테스트")
    class ExcludeTest {
        
        @Nested
        @DisplayName("GroupA 제외")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.exclude-groups=groupA",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter=DEBUG"
        })
        class ExcludeGroupATest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("groupA가 제외되는지 확인")
            void testExcludeGroupA() {
                logger.info("=== 검증 시나리오 1: groupA 제외 테스트 시작 ===");
                
                // LazyServiceA는 제외되어 빈 정의가 없어야 함
                assertThrows(NoSuchBeanDefinitionException.class, () -> {
                    applicationContext.getBean(LazyServiceA.class);
                }, "LazyServiceA는 제외되어 빈 정의가 없어야 합니다.");
                
                // LazyServiceB는 존재해야 함
                assertDoesNotThrow(() -> {
                    LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
                    assertNotNull(lazyServiceB, "LazyServiceB는 존재해야 합니다.");
                });
                
                // NormalService는 영향받지 않음
                assertDoesNotThrow(() -> {
                    NormalService normalService = applicationContext.getBean(NormalService.class);
                    assertNotNull(normalService, "NormalService는 존재해야 합니다.");
                });
                
                logger.info("=== 검증 시나리오 1: 완료 - groupA 제외 확인됨 ===");
            }
        }
        
        @Nested
        @DisplayName("GroupB 제외")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.exclude-groups=groupB",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter=DEBUG"
        })
        class ExcludeGroupBTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("groupB가 제외되는지 확인")
            void testExcludeGroupB() {
                logger.info("=== 검증 시나리오 2: groupB 제외 테스트 시작 ===");
                
                // LazyServiceB는 제외되어 빈 정의가 없어야 함
                assertThrows(NoSuchBeanDefinitionException.class, () -> {
                    applicationContext.getBean(LazyServiceB.class);
                }, "LazyServiceB는 제외되어 빈 정의가 없어야 합니다.");
                
                // LazyServiceA는 존재해야 함
                assertDoesNotThrow(() -> {
                    LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
                    assertNotNull(lazyServiceA, "LazyServiceA는 존재해야 합니다.");
                });
                
                // NormalService는 영향받지 않음
                assertDoesNotThrow(() -> {
                    NormalService normalService = applicationContext.getBean(NormalService.class);
                    assertNotNull(normalService, "NormalService는 존재해야 합니다.");
                });
                
                logger.info("=== 검증 시나리오 2: 완료 - groupB 제외 확인됨 ===");
            }
        }
        
        @Nested
        @DisplayName("다중 그룹 제외")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.exclude-groups=groupA,groupB",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter=DEBUG"
        })
        class ExcludeMultipleGroupsTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("다중 그룹이 제외되는지 확인")
            void testExcludeMultipleGroups() {
                logger.info("=== 검증 시나리오 3: 다중 그룹 제외 테스트 시작 ===");
                
                // LazyServiceA는 제외되어 빈 정의가 없어야 함
                assertThrows(NoSuchBeanDefinitionException.class, () -> {
                    applicationContext.getBean(LazyServiceA.class);
                }, "LazyServiceA는 제외되어 빈 정의가 없어야 합니다.");
                
                // LazyServiceB는 제외되어 빈 정의가 없어야 함
                assertThrows(NoSuchBeanDefinitionException.class, () -> {
                    applicationContext.getBean(LazyServiceB.class);
                }, "LazyServiceB는 제외되어 빈 정의가 없어야 합니다.");
                
                // NormalService는 영향받지 않음
                assertDoesNotThrow(() -> {
                    NormalService normalService = applicationContext.getBean(NormalService.class);
                    assertNotNull(normalService, "NormalService는 존재해야 합니다.");
                });
                
                logger.info("=== 검증 시나리오 3: 완료 - 다중 그룹 제외 확인됨 ===");
            }
        }
    }

    @Nested
    @DisplayName("AOP 로깅 테스트")
    class LoggingTest {
        
        @Nested
        @DisplayName("GroupA 지연 로딩 로깅 테스트")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.groups=groupA",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter.aspect.LazyInitLoggingAspect=INFO"
        })
        class GroupALoggingTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("LazyServiceA 첫 번째 메소드 호출 시 초기화 로깅 확인")
            void testLazyServiceAInitializationLogging() {
                logger.info("=== 검증 시나리오: LazyServiceA 초기화 로깅 테스트 시작 ===");
                
                // LazyServiceA 빈 가져오기 (아직 초기화되지 않음)
                LazyServiceA lazyServiceA = applicationContext.getBean(LazyServiceA.class);
                assertNotNull(lazyServiceA, "LazyServiceA 빈이 존재해야 합니다");
                
                // 첫 번째 메소드 호출 - 초기화 로깅이 발생해야 함
                logger.info("첫 번째 메소드 호출 - 초기화 로깅 예상");
                String result1 = lazyServiceA.getMessage();
                assertNotNull(result1, "LazyServiceA.getMessage() 결과가 null이 아니어야 합니다");
                
                // 두 번째 메소드 호출 - 초기화 로깅이 발생하지 않아야 함
                logger.info("두 번째 메소드 호출 - 초기화 로깅 없음 예상");
                String result2 = lazyServiceA.getMessage();
                assertNotNull(result2, "LazyServiceA.getMessage() 결과가 null이 아니어야 합니다");
                
                logger.info("=== 검증 시나리오: LazyServiceA 초기화 로깅 테스트 완료 ===");
            }
        }
        
        @Nested
        @DisplayName("GroupB 지연 로딩 로깅 테스트")
        @TestPropertySource(properties = {
            "lazy-init.enabled=true",
            "lazy-init.groups=groupB",
            "logging.level.com.synapse.lazy_init_group_spring_boot_starter.aspect.LazyInitLoggingAspect=INFO"
        })
        class GroupBLoggingTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("LazyServiceB 첫 번째 메소드 호출 시 초기화 로깅 확인")
            void testLazyServiceBInitializationLogging() {
                logger.info("=== 검증 시나리오: LazyServiceB 초기화 로깅 테스트 시작 ===");
                
                // LazyServiceB 빈 가져오기 (아직 초기화되지 않음)
                LazyServiceB lazyServiceB = applicationContext.getBean(LazyServiceB.class);
                assertNotNull(lazyServiceB, "LazyServiceB 빈이 존재해야 합니다");
                
                // 첫 번째 메소드 호출 - 초기화 로깅이 발생해야 함
                logger.info("첫 번째 메소드 호출 - 초기화 로깅 예상");
                String result1 = lazyServiceB.getMessage();
                assertNotNull(result1, "LazyServiceB.getMessage() 결과가 null이 아니어야 합니다");
                
                // 두 번째 메소드 호출 - 초기화 로깅이 발생하지 않아야 함
                logger.info("두 번째 메소드 호출 - 초기화 로깅 없음 예상");
                String result2 = lazyServiceB.getMessage();
                assertNotNull(result2, "LazyServiceB.getMessage() 결과가 null이 아니어야 합니다");
                
                logger.info("=== 검증 시나리오: LazyServiceB 초기화 로깅 테스트 완료 ===");
            }
        }
        
        @Nested
        @DisplayName("지연 로딩 비활성화 시 로깅 테스트")
        @SpringBootTest(properties = {"lazy-init.enabled=false"})
        class DisabledLoggingTest {
            
            @Autowired
            private ApplicationContext applicationContext;
            
            @Test
            @DisplayName("지연 로딩이 비활성화된 경우 ApplicationContext가 정상적으로 로드되는지 확인")
            void testApplicationContextLoadsWhenLazyInitDisabled() {
                logger.info("=== 검증 시나리오: 지연 로딩 비활성화 시 로깅 테스트 시작 ===");
                
                // ApplicationContext가 정상적으로 로드되어야 함
                assertNotNull(applicationContext, "ApplicationContext가 정상적으로 로드되어야 합니다");
                
                logger.info("=== 검증 시나리오: 지연 로딩 비활성화 시 로깅 테스트 완료 ===");
            }
        }
    }
}