package com.khanabook.saas.controller;

import com.khanabook.saas.entity.BillItem;
import com.khanabook.saas.service.BillItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.khanabook.saas.security.TenantContext;

@RestController
@RequestMapping("/sync/bills/items")
@RequiredArgsConstructor
public class BillItemController {
    private final BillItemService service;

    @PostMapping("/push")
    public ResponseEntity<?> push(@RequestBody List<BillItem> payload) {
        try {
            System.out.println("\n[BillItemController] Received push for " + payload.size() + " items for Tenant: " + TenantContext.getCurrentTenant());
            return ResponseEntity.ok(service.pushData(TenantContext.getCurrentTenant(), payload));
        } catch (Exception e) {
            System.err.println("[BillItemController] Error pushing data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/pull")
    public ResponseEntity<List<BillItem>> pull(
            @RequestParam Long lastSyncTimestamp,
            @RequestParam String deviceId) {
        return ResponseEntity.ok(service.pullData(TenantContext.getCurrentTenant(), lastSyncTimestamp, deviceId));
    }
}
