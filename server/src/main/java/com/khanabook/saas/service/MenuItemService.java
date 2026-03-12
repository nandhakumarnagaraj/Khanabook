package com.khanabook.saas.service;

import com.khanabook.saas.entity.MenuItem;
import java.util.List;

public interface MenuItemService {
    List<Integer> pushData(Long tenantId, List<MenuItem> payload);
    List<MenuItem> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId);
}
