package com.khanabook.saas.service.impl;

import com.khanabook.saas.entity.BillItem;
import com.khanabook.saas.repository.BillItemRepository;
import com.khanabook.saas.service.BillItemService;
import com.khanabook.saas.sync.service.GenericSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillItemServiceImpl implements BillItemService {
    private final BillItemRepository repository;
    private final GenericSyncService genericSyncService;

    @Override
    public List<Integer> pushData(Long tenantId, List<BillItem> payload) {
        return genericSyncService.handlePushSync(tenantId, payload, repository);
    }

    @Override
    public List<BillItem> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId) {
        return repository.findByRestaurantIdAndUpdatedAtGreaterThanAndDeviceIdNot(tenantId, lastSyncTimestamp, deviceId);
    }
}
