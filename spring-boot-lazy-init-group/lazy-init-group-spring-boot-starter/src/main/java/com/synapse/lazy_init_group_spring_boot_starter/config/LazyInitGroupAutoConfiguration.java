package com.synapse.lazy_init_group_spring_boot_starter.config;

import com.synapse.lazy_init_group_spring_boot_starter.processor.ExcludeGroupBeanDefinitionRegistryPostProcessor;
import com.synapse.lazy_init_group_spring_boot_starter.processor.LazyInitGroupBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@AutoConfiguration
@Import(LazyInitAspectAutoConfiguration.class)
public class LazyInitGroupAutoConfiguration {

    @Bean
    public ExcludeGroupBeanDefinitionRegistryPostProcessor excludeGroupBeanDefinitionRegistryPostProcessor(Environment environment) {
        return new ExcludeGroupBeanDefinitionRegistryPostProcessor(environment);
    }

    @Bean
    public LazyInitGroupBeanFactoryPostProcessor lazyInitGroupBeanFactoryPostProcessor(Environment environment) {
        return new LazyInitGroupBeanFactoryPostProcessor(environment);
    }
}
