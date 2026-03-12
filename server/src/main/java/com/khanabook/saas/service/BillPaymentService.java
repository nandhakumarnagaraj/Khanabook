package com.khanabook.saas.service;

import com.khanabook.saas.entity.BillPayment;
import java.util.List;

public interface BillPaymentService {
    List<Integer> pushData(Long tenantId, List<BillPayment> payload);
    List<BillPayment> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId);
}
