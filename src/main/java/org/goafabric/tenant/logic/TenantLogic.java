package org.goafabric.tenant.logic;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TenantLogic {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Value("${multi-tenancy.tenants}")
    private String tenants;

    @Value("${application.images}")
    private String applicationImages;


    @PostConstruct
    public void init() {
        Arrays.asList(tenants.split(",")).forEach(tenant -> log.info("#tenant: {}", tenant));
        Arrays.asList(applicationImages.split(",")).forEach(application -> log.info("#application: {}", application));
    }
}
