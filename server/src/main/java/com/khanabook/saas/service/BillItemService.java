package com.khanabook.saas.service;

import com.khanabook.saas.entity.BillItem;
import java.util.List;

public interface BillItemService {
    List<Integer> pushData(Long tenantId, List<BillItem> payload);
    List<BillItem> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId);
}
