package org.goafabric.tenant.logic;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TenantLogic {
    private List<String> tenants = new ArrayList<>();

    @Autowired
    private ProvisionLogic provisionLogic;

    @PostConstruct
    public void init() {
        //addTenant("4711");
    }

    public void addTenant(String tenant) {
        tenants.add(tenant);
        provisionLogic.executeYo(tenant);
    }

    public List<String> getTenants() {
        return tenants;
    }

    public void deleteTenant(String tenant) {
        tenants.remove(tenant);
    }
}
