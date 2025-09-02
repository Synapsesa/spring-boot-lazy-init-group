package com.synapse.lazy_init_group_spring_boot_starter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NormalService {
    
    private static final Logger logger = LoggerFactory.getLogger(NormalService.class);
    
    public NormalService() {
        logger.info("[INIT] NormalService 생성자 호출됨 - 즉시 로딩");
    }
    
    public String getMessage() {
        return "Normal Service Message";
    }
}