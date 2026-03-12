package com.khanabook.saas.service;

import com.khanabook.saas.entity.Bill;
import java.util.List;

public interface BillService {
    List<Integer> pushData(Long tenantId, List<Bill> payload);
    List<Bill> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId);
}
