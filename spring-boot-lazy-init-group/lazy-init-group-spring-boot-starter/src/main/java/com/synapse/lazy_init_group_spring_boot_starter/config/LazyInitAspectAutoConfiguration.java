package com.synapse.lazy_init_group_spring_boot_starter.config;

import com.synapse.lazy_init_group_spring_boot_starter.aspect.LazyInitLoggingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@ConditionalOnProperty(name = "lazy-init.enabled", havingValue = "true")
@EnableAspectJAutoProxy
public class LazyInitAspectAutoConfiguration {
    
    @Bean
    public LazyInitLoggingAspect lazyInitLoggingAspect() {
        return new LazyInitLoggingAspect();
    }
}
