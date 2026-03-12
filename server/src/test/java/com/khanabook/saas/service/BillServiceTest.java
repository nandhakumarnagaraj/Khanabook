package com.khanabook.saas.service;

import com.khanabook.saas.entity.Bill;
import com.khanabook.saas.repository.BillRepository;
import com.khanabook.saas.service.impl.BillServiceImpl;
import com.khanabook.saas.sync.service.GenericSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Spy
    private GenericSyncService genericSyncService; // Use real sync logic but can mock repository

    @InjectMocks
    private BillServiceImpl billService;

    @Captor
    private ArgumentCaptor<Bill> billCaptor;

    private final Long AUTHENTICATED_RESTAURANT_ID = 99L;
    private final String DEVICE_ID = "TABLET_1";

    private Bill createMobileBill(Integer localId, Long updatedAt) {
        Bill bill = new Bill();
        bill.setLocalId(localId);
        bill.setUpdatedAt(updatedAt);
        bill.setDeviceId(DEVICE_ID);
        return bill;
    }

    @Test
    void givenExistingBill_whenMobileIsNewer_thenUpdateLwwSuccess() {
        Long oldServerTime = 1000L;
        Long newMobileTime = 2000L;

        Bill existingDbBill = new Bill();
        existingDbBill.setId(5L);
        existingDbBill.setUpdatedAt(oldServerTime);
        existingDbBill.setDeviceId(DEVICE_ID);
        existingDbBill.setLocalId(101);

        Bill mobileBill = createMobileBill(101, newMobileTime);

        when(billRepository.findByRestaurantIdAndDeviceIdAndLocalId(
                eq(AUTHENTICATED_RESTAURANT_ID), eq(DEVICE_ID), eq(101)))
                .thenReturn(Optional.of(existingDbBill));

        List<Integer> successIds = billService.pushData(AUTHENTICATED_RESTAURANT_ID, List.of(mobileBill));

        verify(billRepository).save(billCaptor.capture());
        Bill savedBill = billCaptor.getValue();

        assertThat(savedBill.getId()).isEqualTo(5L);
        assertThat(savedBill.getUpdatedAt()).isEqualTo(newMobileTime);
        assertThat(successIds).containsExactly(101);
    }

    @Test
    void givenHackedPayload_whenInsertNewBill_thenForceTenantIsolation() {
        Long maliciousRestaurantId = 666L;
        Bill hackedMobileBill = createMobileBill(202, 1000L);
        hackedMobileBill.setRestaurantId(maliciousRestaurantId);

        when(billRepository.findByRestaurantIdAndDeviceIdAndLocalId(anyLong(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        billService.pushData(AUTHENTICATED_RESTAURANT_ID, List.of(hackedMobileBill));

        verify(billRepository).save(billCaptor.capture());
        Bill savedBill = billCaptor.getValue();

        // Server MUST override the malicious ID with the authenticated one
        assertThat(savedBill.getRestaurantId()).isEqualTo(AUTHENTICATED_RESTAURANT_ID);
    }
}
