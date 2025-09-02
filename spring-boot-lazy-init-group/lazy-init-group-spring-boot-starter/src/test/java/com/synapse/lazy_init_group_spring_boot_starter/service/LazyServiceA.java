package com.synapse.lazy_init_group_spring_boot_starter.service;

import com.synapse.lazy_init_group_api.annotation.LazyInitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@LazyInitGroup("groupA")
public class LazyServiceA {
    
    private static final Logger logger = LoggerFactory.getLogger(LazyServiceA.class);
    
    public LazyServiceA() {
        logger.info("[INIT] LazyServiceA 생성자 호출됨 - 지연 로딩");
    }
    
    public String getMessage() {
        return "Lazy Service A Message";
    }
}