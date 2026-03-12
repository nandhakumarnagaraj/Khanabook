package com.khanabook.saas.controller;

import com.khanabook.saas.entity.ItemVariant;
import com.khanabook.saas.service.ItemVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.khanabook.saas.security.TenantContext;

@RestController
@RequestMapping("/sync/itemvariant")
@RequiredArgsConstructor
public class ItemVariantController {
    private final ItemVariantService service;

    @PostMapping("/push")
    public ResponseEntity<List<Integer>> push(@RequestBody List<ItemVariant> payload) {
        // TenantId automatically extracted by JwtFilter
        return ResponseEntity.ok(service.pushData(TenantContext.getCurrentTenant(), payload));
    }

    @GetMapping("/pull")
    public ResponseEntity<List<ItemVariant>> pull(
            @RequestParam Long lastSyncTimestamp,
            @RequestParam String deviceId) {
        return ResponseEntity.ok(service.pullData(TenantContext.getCurrentTenant(), lastSyncTimestamp, deviceId));
    }
}
