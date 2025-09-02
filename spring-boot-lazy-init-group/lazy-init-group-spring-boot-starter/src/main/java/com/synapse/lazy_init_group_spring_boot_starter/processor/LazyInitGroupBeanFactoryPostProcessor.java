package com.synapse.lazy_init_group_spring_boot_starter.processor;

import com.synapse.lazy_init_group_api.annotation.LazyInitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LazyInitGroupBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(LazyInitGroupBeanFactoryPostProcessor.class);
    
    private final Environment environment;
    
    public LazyInitGroupBeanFactoryPostProcessor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.debug("LazyInitGroupBeanFactoryPostProcessor.postProcessBeanFactory 시작");
        
        List<String> groups = getGroups();
        
        logger.debug("groups: {}", groups);
        
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        
        logger.debug("지연 초기화할 그룹 목록: {}", groups);
        
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        int processedCount = 0;
        
        for (String beanName : beanNames) {
            try {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                
                Optional<String> groupName = extractLazyInitGroupName(beanDefinition);
                
                if (groupName.isPresent() && groups.contains(groupName.get())) {
                    beanDefinition.setLazyInit(true);
                    processedCount++;
                    
                    logger.debug("빈 '{}' (그룹: '{}')의 지연 초기화가 설정되었습니다.", 
                               beanName, groupName.get());
                }
            } catch (Exception e) {
                logger.debug("빈 '{}' 처리 중 오류 발생: {}", beanName, e.getMessage());
            }
        }
        
        logger.debug("LazyInitGroupBeanFactoryPostProcessor.postProcessBeanFactory 완료: 총 {} 개의 빈에 지연 초기화를 설정했습니다.", processedCount);
    }
    
    private Optional<String> extractLazyInitGroupName(BeanDefinition beanDefinition) {
        try {
            Class<?> beanClass = getBeanClass(beanDefinition);
            if (beanClass != null) {
                LazyInitGroup annotation = beanClass.getAnnotation(LazyInitGroup.class);
                if (annotation != null) {
                    return Optional.of(annotation.value());
                }
            }
        } catch (Exception e) {
            logger.debug("@LazyInitGroup 그룹명 추출 중 오류: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Class<?> getBeanClass(BeanDefinition beanDefinition) {
        try {
            Class<?> resolvedClass = beanDefinition.getResolvableType().resolve();
            
            // CGLIB 프록시가 적용된 경우 원본 클래스를 안전하게 조회
            if (resolvedClass != null && resolvedClass != Object.class) {
                Class<?> userClass = ClassUtils.getUserClass(resolvedClass);
                logger.debug("ResolvableType으로 클래스 타입 조회 성공: {} (원본: {})", 
                           resolvedClass.getName(), userClass.getName());
                return userClass;
            }
            
            // 일반적인 빈
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> beanClass = Class.forName(beanClassName);
                logger.debug("getBeanClassName()으로 클래스 타입 조회 성공: {}", beanClass.getName());
                return beanClass;
            }
            
            if (beanDefinition.getFactoryBeanName() != null) {
                logger.debug("팩토리 빈 '{}' 감지, 클래스 타입을 직접 확인할 수 없습니다. 팩토리 메서드: {}", 
                           beanDefinition.getFactoryBeanName(), beanDefinition.getFactoryMethodName());
            }
            
        } catch (ClassNotFoundException e) {
            logger.debug("클래스를 찾을 수 없습니다: {} - {}", beanDefinition.getBeanClassName(), e.getMessage());
        } catch (Exception e) {
            logger.debug("빈 클래스 타입 확인 중 오류: {} - ResolvableType: {}, BeanClassName: {}", 
                       e.getMessage(), beanDefinition.getResolvableType(), beanDefinition.getBeanClassName());
        }
        
        return null;
    }
    
    private List<String> getGroups() {
        String groupsStr = environment.getProperty("lazy-init.groups");
        if (StringUtils.hasText(groupsStr)) {
            return Arrays.asList(groupsStr.split(","));
        }
        return Collections.emptyList();
    }
}
