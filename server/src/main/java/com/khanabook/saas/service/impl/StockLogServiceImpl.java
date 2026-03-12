package com.khanabook.saas.service.impl;

import com.khanabook.saas.entity.MenuItem;
import com.khanabook.saas.entity.StockLog;
import com.khanabook.saas.repository.MenuItemRepository;
import com.khanabook.saas.repository.StockLogRepository;
import com.khanabook.saas.service.StockLogService;
import com.khanabook.saas.sync.service.GenericSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockLogServiceImpl implements StockLogService {
    private final StockLogRepository repository;
    private final MenuItemRepository menuItemRepository;
    private final GenericSyncService genericSyncService;

    @Override
    public List<Integer> pushData(Long tenantId, List<StockLog> payload) {
        // Resolve Server IDs for menu items
        for (StockLog log : payload) {
            if (log.getServerMenuItemId() == null && log.getMenuItemId() != null) {
                Optional<MenuItem> menuItem = menuItemRepository.findByRestaurantIdAndDeviceIdAndLocalId(
                        tenantId, log.getDeviceId(), log.getMenuItemId());
                menuItem.ifPresent(item -> log.setServerMenuItemId(item.getId()));
                
                // Fallback: If not found by device (e.g. created on another device), try just tenant + localId
                // But the system seems designed for device-specific localIds.
                // If still null, we might need a default to avoid DB crash.
                if (log.getServerMenuItemId() == null) {
                    log.setServerMenuItemId(0L); // Placeholder to satisfy NOT NULL
                }
            }
        }
        return genericSyncService.handlePushSync(tenantId, payload, repository);
    }

    @Override
    public List<StockLog> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId) {
        return repository.findByRestaurantIdAndUpdatedAtGreaterThanAndDeviceIdNot(tenantId, lastSyncTimestamp, deviceId);
    }
}
