package com.khanabook.saas.service.impl;

import com.khanabook.saas.entity.RestaurantProfile;
import com.khanabook.saas.repository.RestaurantProfileRepository;
import com.khanabook.saas.service.RestaurantProfileService;
import com.khanabook.saas.sync.service.GenericSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantProfileServiceImpl implements RestaurantProfileService {
    private final RestaurantProfileRepository repository;
    private final GenericSyncService genericSyncService;

    @Override
    public List<Integer> pushData(Long tenantId, List<RestaurantProfile> payload) {
        return genericSyncService.handlePushSync(tenantId, payload, repository);
    }

    @Override
    public List<RestaurantProfile> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId) {
        return repository.findByRestaurantIdAndUpdatedAtGreaterThanAndDeviceIdNot(tenantId, lastSyncTimestamp, deviceId);
    }

    @Override
    @Transactional
    public synchronized CounterResponse incrementAndGetCounters(Long tenantId, String today) {
        RestaurantProfile profile = repository.findByRestaurantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Restaurant profile not found"));

        Integer nextDaily;
        if (profile.getLastResetDate() == null || !profile.getLastResetDate().equals(today)) {
            nextDaily = 1;
        } else {
            nextDaily = (profile.getDailyOrderCounter() == null ? 0 : profile.getDailyOrderCounter()) + 1;
        }

        Integer nextLifetime = (profile.getLifetimeOrderCounter() == null ? 0 : profile.getLifetimeOrderCounter()) + 1;
        Long now = System.currentTimeMillis();
        
        repository.updateCountersAtomic(tenantId, nextDaily, nextLifetime, today, now);

        CounterResponse response = new CounterResponse();
        response.setDailyCounter(nextDaily);
        response.setLifetimeCounter(nextLifetime);
        return response;
    }
}
