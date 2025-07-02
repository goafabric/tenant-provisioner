package org.goafabric.tenant.controller;

import org.goafabric.tenant.logic.TenantLogic;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "tenants", produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantController {
    private final TenantLogic tenantLogic;

    public TenantController(TenantLogic tenantLogic) {
        this.tenantLogic = tenantLogic;
    }

    @GetMapping("/")
    public List<String> getTenants() {
        return tenantLogic.getTenants();
    }

    @DeleteMapping("/{tenant}")
    public void deleteTenant(@PathVariable("tenant") String tenant) {
        tenantLogic.deleteTenant(tenant);
    }

    @PostMapping("/{tenant}")
    public void addTenant(@PathVariable("tenant") String tenant) {
        tenantLogic.addTenant(tenant);
    }


}

