package com.khanabook.saas.service.impl;

import com.khanabook.saas.entity.BillPayment;
import com.khanabook.saas.repository.BillPaymentRepository;
import com.khanabook.saas.service.BillPaymentService;
import com.khanabook.saas.sync.service.GenericSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillPaymentServiceImpl implements BillPaymentService {
    private final BillPaymentRepository repository;
    private final GenericSyncService genericSyncService;

    @Override
    public List<Integer> pushData(Long tenantId, List<BillPayment> payload) {
        return genericSyncService.handlePushSync(tenantId, payload, repository);
    }

    @Override
    public List<BillPayment> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId) {
        return repository.findByRestaurantIdAndUpdatedAtGreaterThanAndDeviceIdNot(tenantId, lastSyncTimestamp, deviceId);
    }
}
