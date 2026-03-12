package com.khanabook.saas.repository;

import com.khanabook.saas.entity.RestaurantProfile;
import com.khanabook.saas.sync.repository.SyncRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RestaurantProfileRepository extends SyncRepository<RestaurantProfile, Long> {
    Optional<RestaurantProfile> findByRestaurantId(Long restaurantId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE restaurantprofiles SET daily_order_counter = :daily, lifetime_order_counter = :lifetime, last_reset_date = :today, updated_at = :now, server_updated_at = :now WHERE restaurant_id = :restaurantId", nativeQuery = true)
    void updateCountersAtomic(Long restaurantId, Integer daily, Integer lifetime, String today, Long now);
}
