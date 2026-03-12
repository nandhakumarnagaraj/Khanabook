package com.khanabook.lite.pos.domain.manager

import android.util.Log
import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.remote.api.KhanaBookApi
import com.khanabook.lite.pos.data.remote.api.MasterSyncResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val api: KhanaBookApi,
    private val billDao: BillDao,
    private val restaurantDao: RestaurantDao,
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val menuDao: MenuDao,
    private val inventoryDao: InventoryDao
) {

    suspend fun performMasterPull(): Result<Unit> {
        val deviceId = sessionManager.getDeviceId() ?: return Result.failure(Exception("No device ID"))
        val lastSyncTimestamp = sessionManager.getLastSyncTimestamp()

        return try {
            val masterData = api.pullMasterSync(lastSyncTimestamp, deviceId)
            insertMasterData(masterData)
            sessionManager.saveLastSyncTimestamp(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncManager", "Master pull failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun pushUnsyncedDataImmediately(): Boolean {
        return try {
            val unsyncedBills = billDao.getUnsyncedBills()
            if (unsyncedBills.isNotEmpty()) {
                val syncedIds = api.pushBills(unsyncedBills)
                billDao.markBillsAsSynced(syncedIds)
            }

            val unsyncedBillItems = billDao.getUnsyncedBillItems()
            if (unsyncedBillItems.isNotEmpty()) {
                val syncedIds = api.pushBillItems(unsyncedBillItems)
                billDao.markBillItemsAsSynced(syncedIds)
            }

            val unsyncedBillPayments = billDao.getUnsyncedBillPayments()
            if (unsyncedBillPayments.isNotEmpty()) {
                val syncedIds = api.pushBillPayments(unsyncedBillPayments)
                billDao.markBillPaymentsAsSynced(syncedIds)
            }

            true
        } catch (e: Exception) {
            Log.e("SyncManager", "Push failed", e)
            false
        }
    }

    private suspend fun insertMasterData(masterData: MasterSyncResponse) {
        if (masterData.profiles.isNotEmpty()) {
            val currentLocalProfile = restaurantDao.getProfile()
            restaurantDao.insertSyncedRestaurantProfiles(masterData.profiles.map { 
                it.copy(
                    id = 1, 
                    isSynced = true,
                    dailyOrderCounter = currentLocalProfile?.dailyOrderCounter ?: it.dailyOrderCounter,
                    lifetimeOrderCounter = currentLocalProfile?.lifetimeOrderCounter ?: it.lifetimeOrderCounter,
                    lastResetDate = currentLocalProfile?.lastResetDate ?: it.lastResetDate
                ) 
            })
        }
        if (masterData.users.isNotEmpty()) {
            userDao.insertSyncedUsers(masterData.users.map { it.copy(isSynced = true) })
        }
        if (masterData.categories.isNotEmpty()) {
            categoryDao.insertSyncedCategories(masterData.categories.map { it.copy(isSynced = true) })
        }
        if (masterData.menuItems.isNotEmpty()) {
            menuDao.insertSyncedMenuItems(masterData.menuItems.map { it.copy(isSynced = true) })
        }
        if (masterData.itemVariants.isNotEmpty()) {
            menuDao.insertSyncedItemVariants(masterData.itemVariants.map { it.copy(isSynced = true) })
        }
        if (masterData.stockLogs.isNotEmpty()) {
            inventoryDao.insertSyncedStockLogs(masterData.stockLogs.map { it.copy(isSynced = true) })
        }
        if (masterData.bills.isNotEmpty()) {
            billDao.insertSyncedBills(masterData.bills.map { it.copy(isSynced = true) })
        }
        if (masterData.billItems.isNotEmpty()) {
            billDao.insertSyncedBillItems(masterData.billItems.map { it.copy(isSynced = true) })
        }
        if (masterData.billPayments.isNotEmpty()) {
            billDao.insertSyncedBillPayments(masterData.billPayments.map { it.copy(isSynced = true) })
        }
    }
}
