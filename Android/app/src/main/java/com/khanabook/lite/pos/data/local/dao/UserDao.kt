package com.khanabook.lite.pos.data.local.dao

import androidx.room.*
import com.khanabook.lite.pos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): UserEntity?

    @Query("UPDATE users SET password_hash = :newHash WHERE id = :userId")
    suspend fun updatePasswordHash(userId: Int, newHash: String)

    @Query("UPDATE users SET email = :newPhone, whatsapp_number = :newPhone")
    suspend fun updateAdminPhoneNumber(newPhone: String)

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET is_active = :isActive WHERE id = :userId")
    suspend fun setActivationStatus(userId: Int, isActive: Boolean)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE is_synced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("UPDATE users SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markUsersAsSynced(ids: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncedUsers(items: List<UserEntity>)

    @Query("SELECT * FROM users")
    suspend fun getAllUsersOnce(): List<UserEntity>
}
