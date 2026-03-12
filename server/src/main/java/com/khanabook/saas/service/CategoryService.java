package com.khanabook.saas.service;

import com.khanabook.saas.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Integer> pushData(Long tenantId, List<Category> payload);
    List<Category> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId);
}
