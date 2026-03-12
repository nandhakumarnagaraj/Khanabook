package com.khanabook.lite.pos.data.local.dao

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.RestaurantProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: RestaurantProfileEntity)

    @Query("SELECT * FROM restaurant_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): RestaurantProfileEntity?

    @Query("SELECT * FROM restaurant_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<RestaurantProfileEntity?>

    @Query("UPDATE restaurant_profile SET daily_order_counter = :counter, last_reset_date = :date WHERE id = 1")
    suspend fun resetDailyCounter(counter: Int, date: String)

    @Query("UPDATE restaurant_profile SET daily_order_counter = daily_order_counter + 1, lifetime_order_counter = lifetime_order_counter + 1 WHERE id = 1")
    suspend fun incrementOrderCounters()

    @Transaction
    suspend fun incrementAndGetCounters(today: String): Pair<Int, Int> {
        val profile = getProfile() ?: throw Exception("Profile not found")
        val isNewDay = profile.lastResetDate != today
        
        val nextDaily = if (isNewDay) 1 else profile.dailyOrderCounter + 1
        val nextLifetime = profile.lifetimeOrderCounter + 1
        
        if (isNewDay) {
            resetDailyCounter(nextDaily, today)
            // Still need to increment lifetime
            updateLifetimeCounter(nextLifetime)
        } else {
            incrementOrderCounters()
        }
        
        return Pair(nextDaily, nextLifetime)
    }

    @Transaction
    suspend fun updateCounters(daily: Int, lifetime: Int, today: String) {
        val current = getProfile() ?: return
        saveProfile(current.copy(
            dailyOrderCounter = daily,
            lifetimeOrderCounter = lifetime,
            lastResetDate = today,
            isSynced = true,
            updatedAt = System.currentTimeMillis()
        ))
    }

    @Query("UPDATE restaurant_profile SET lifetime_order_counter = :counter WHERE id = 1")
    suspend fun updateLifetimeCounter(counter: Int)

    @Query("UPDATE restaurant_profile SET upi_qr_path = :path WHERE id = 1")
    suspend fun updateUpiQrPath(path: String?)

    @Query("UPDATE restaurant_profile SET logo_path = :path WHERE id = 1")
    suspend fun updateLogoPath(path: String?)

    @Query("SELECT * FROM restaurant_profile WHERE is_synced = 0")
    suspend fun getUnsyncedRestaurantProfiles(): List<RestaurantProfileEntity>

    @Query("UPDATE restaurant_profile SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markRestaurantProfilesAsSynced(ids: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncedRestaurantProfiles(items: List<RestaurantProfileEntity>)
}
