package com.synapse.lazy_init_group_spring_boot_starter.processor;

import com.synapse.lazy_init_group_api.annotation.LazyInitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExcludeGroupBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExcludeGroupBeanDefinitionRegistryPostProcessor.class);
    private final Environment environment;

    public ExcludeGroupBeanDefinitionRegistryPostProcessor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        logger.debug("ExcludeGroupBeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry 시작");
        
        List<String> excludeGroups = getExcludeGroups();
        
        if (CollectionUtils.isEmpty(excludeGroups)) {
            return;
        }
        
        logger.debug("제외할 그룹 목록: {}", excludeGroups);
        
        String[] beanNames = registry.getBeanDefinitionNames();
        int removedCount = 0;
        
        for (String beanName : beanNames) {
            try {
                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                
                Optional<String> groupName = extractLazyInitGroupName(beanDefinition);
                
                if (groupName.isPresent() && excludeGroups.contains(groupName.get())) {
                    registry.removeBeanDefinition(beanName);
                    removedCount++;
                    
                    logger.debug("빈 '{}' (그룹: '{}')이 제외 그룹에 포함되어 제거되었습니다.", 
                               beanName, groupName.get());
                }
            } catch (Exception e) {
                logger.debug("빈 '{}' 처리 중 오류 발생: {}", beanName, e.getMessage());
            }
        }
        
        logger.debug("ExcludeGroupBeanDefinitionRegistryPostProcessor 완료: 총 {} 개의 빈 정의를 제거했습니다.", removedCount);
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
            // 프록시로 주입된 객체의 경우 이 경우에서 실제 클래스를 추출
            if (resolvedClass != null && resolvedClass != Object.class) {
                Class<?> userClass = ClassUtils.getUserClass(resolvedClass);
                logger.debug("ResolvableType으로 클래스 타입 조회 성공: {} (원본: {})", 
                           resolvedClass.getName(), userClass.getName());
                return userClass;
            }
            
            //흔히 우리가 사용하는 Service, Component 등등..
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

    private List<String> getExcludeGroups() {
        String excludeGroupsStr = environment.getProperty("lazy-init.exclude-groups");
        if (StringUtils.hasText(excludeGroupsStr)) {
            return Arrays.asList(excludeGroupsStr.split(","));
        }
        return Collections.emptyList();
    }
}