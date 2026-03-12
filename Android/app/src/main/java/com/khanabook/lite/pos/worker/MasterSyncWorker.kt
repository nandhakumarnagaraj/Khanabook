package com.khanabook.lite.pos.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.local.entity.*
import com.khanabook.lite.pos.data.remote.api.KhanaBookApi
import com.khanabook.lite.pos.data.remote.api.MasterSyncResponse
import com.khanabook.lite.pos.domain.manager.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class MasterSyncWorker
@AssistedInject
constructor(
        @Assisted private val context: Context,
        @Assisted private val workerParams: WorkerParameters,
        private val api: KhanaBookApi,
        private val sessionManager: SessionManager,
        private val billDao: BillDao,
        private val restaurantDao: RestaurantDao,
        private val userDao: UserDao,
        private val categoryDao: CategoryDao,
        private val menuDao: MenuDao,
        private val inventoryDao: InventoryDao
) : CoroutineWorker(context, workerParams) {

  companion object {
    private const val SYNC_WORK_NAME = "MasterSyncWorker"

    fun schedule(context: Context) {
      val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

      val syncRequest =
              PeriodicWorkRequestBuilder<MasterSyncWorker>(
                              15,
                              TimeUnit.MINUTES
                      )
                      .setConstraints(constraints)
                      .setBackoffCriteria(
                          androidx.work.BackoffPolicy.EXPONENTIAL,
                          30,
                          TimeUnit.SECONDS
                      )
                      .build()

      WorkManager.getInstance(context)
              .enqueueUniquePeriodicWork(
                      SYNC_WORK_NAME,
                      ExistingPeriodicWorkPolicy.KEEP,
                      syncRequest
              )
    }
  }

  override suspend fun doWork(): Result =
          withContext(Dispatchers.IO) {
            val token = sessionManager.getAuthToken()

            if (token.isNullOrBlank() || token == "null" || !token.contains(".")) {
              Log.w("MasterSyncWorker", "Aborting sync: No valid session token found.")
              return@withContext Result.success()
            }

            val deviceId = sessionManager.getDeviceId() ?: return@withContext Result.failure()
            val lastSyncTimestamp = sessionManager.getLastSyncTimestamp()

              try {
              Log.d("MasterSyncWorker", "Starting sync.")

              // 1. PUSH CONFIG (Users & Profiles)
              val unsyncedProfiles = restaurantDao.getUnsyncedRestaurantProfiles()
              if (unsyncedProfiles.isNotEmpty()) {
                unsyncedProfiles.chunked(50).forEach { batch ->
                  val syncedIds = api.pushRestaurantProfiles(batch)
                  restaurantDao.markRestaurantProfilesAsSynced(syncedIds)
                }
              }

              val unsyncedUsers = userDao.getUnsyncedUsers()
              if (unsyncedUsers.isNotEmpty()) {
                unsyncedUsers.chunked(50).forEach { batch ->
                  val syncedIds = api.pushUsers(batch)
                  userDao.markUsersAsSynced(syncedIds)
                }
              }

              // 2. PUSH MENU (Categories, Items, Variants)
              val unsyncedCategories = categoryDao.getUnsyncedCategories()
              if (unsyncedCategories.isNotEmpty()) {
                unsyncedCategories.chunked(50).forEach { batch ->
                  val syncedIds = api.pushCategories(batch)
                  categoryDao.markCategoriesAsSynced(syncedIds)
                }
              }

              val unsyncedMenuItems = menuDao.getUnsyncedMenuItems()
              if (unsyncedMenuItems.isNotEmpty()) {
                unsyncedMenuItems.chunked(50).forEach { batch ->
                  val syncedIds = api.pushMenuItems(batch)
                  menuDao.markMenuItemsAsSynced(syncedIds)
                }
              }

              val unsyncedVariants = menuDao.getUnsyncedItemVariants()
              if (unsyncedVariants.isNotEmpty()) {
                unsyncedVariants.chunked(50).forEach { batch ->
                  val syncedIds = api.pushItemVariants(batch)
                  menuDao.markItemVariantsAsSynced(syncedIds)
                }
              }

              // 3. PUSH INVENTORY
              val unsyncedStockLogs = inventoryDao.getUnsyncedStockLogs()
              if (unsyncedStockLogs.isNotEmpty()) {
                unsyncedStockLogs.chunked(50).forEach { batch ->
                  val syncedIds = api.pushStockLogs(batch)
                  inventoryDao.markStockLogsAsSynced(syncedIds)
                }
              }

              // 4. SYNC BILLS (Push only part)
              val unsyncedBills = billDao.getUnsyncedBills()
              if (unsyncedBills.isNotEmpty()) {
                unsyncedBills.chunked(50).forEach { batch ->
                  val syncedIds = api.pushBills(batch)
                  billDao.markBillsAsSynced(syncedIds)
                }
              }

              val unsyncedBillItems = billDao.getUnsyncedBillItems()
              if (unsyncedBillItems.isNotEmpty()) {
                unsyncedBillItems.chunked(50).forEach { batch ->
                  val syncedIds = api.pushBillItems(batch)
                  billDao.markBillItemsAsSynced(syncedIds)
                }
              }

              val unsyncedBillPayments = billDao.getUnsyncedBillPayments()
              if (unsyncedBillPayments.isNotEmpty()) {
                unsyncedBillPayments.chunked(50).forEach { batch ->
                  val syncedIds = api.pushBillPayments(batch)
                  billDao.markBillPaymentsAsSynced(syncedIds)
                }
              }

              // 5. UNIFIED MASTER PULL
              Log.d("MasterSyncWorker", "Performing Master Pull")
              val masterData = api.pullMasterSync(lastSyncTimestamp, deviceId)

              // Process Pulled Data
              if (masterData.profiles.isNotEmpty()) {
                val currentLocalProfile = restaurantDao.getProfile()
                restaurantDao.insertSyncedRestaurantProfiles(
                        masterData.profiles.map { 
                            it.copy(
                                id = 1, 
                                isSynced = true,
                                // CRITICAL: Keep local counters to avoid multi-device ID conflicts
                                dailyOrderCounter = currentLocalProfile?.dailyOrderCounter ?: it.dailyOrderCounter,
                                lifetimeOrderCounter = currentLocalProfile?.lifetimeOrderCounter ?: it.lifetimeOrderCounter,
                                lastResetDate = currentLocalProfile?.lastResetDate ?: it.lastResetDate
                            ) 
                        }
                )
              }
              if (masterData.users.isNotEmpty()) {
                userDao.insertSyncedUsers(masterData.users.map { it.copy(isSynced = true) })
              }
              if (masterData.categories.isNotEmpty()) {
                categoryDao.insertSyncedCategories(
                        masterData.categories.map { it.copy(isSynced = true) }
                )
              }
              if (masterData.menuItems.isNotEmpty()) {
                menuDao.insertSyncedMenuItems(masterData.menuItems.map { it.copy(isSynced = true) })
              }
              if (masterData.itemVariants.isNotEmpty()) {
                menuDao.insertSyncedItemVariants(
                        masterData.itemVariants.map { it.copy(isSynced = true) }
                )
              }
              if (masterData.stockLogs.isNotEmpty()) {
                inventoryDao.insertSyncedStockLogs(
                        masterData.stockLogs.map { it.copy(isSynced = true) }
                )
              }
              if (masterData.bills.isNotEmpty()) {
                billDao.insertSyncedBills(masterData.bills.map { it.copy(isSynced = true) })
              }
              if (masterData.billItems.isNotEmpty()) {
                billDao.insertSyncedBillItems(masterData.billItems.map { it.copy(isSynced = true) })
              }
              if (masterData.billPayments.isNotEmpty()) {
                billDao.insertSyncedBillPayments(
                        masterData.billPayments.map { it.copy(isSynced = true) }
                )
              }

              sessionManager.saveLastSyncTimestamp(System.currentTimeMillis())
              Log.d("MasterSyncWorker", "Sync completed successfully")

              Result.success()
            } catch (e: Exception) {
              Log.e("MasterSyncWorker", "Sync failed: ${e.message}", e)
              
              // Don't retry if it's a permanent failure (Auth error)
              if (e is retrofit2.HttpException) {
                  if (e.code() == 401 || e.code() == 403) {
                      return@withContext Result.failure()
                  }
              }
              
              if (runAttemptCount > 3) {
                  return@withContext Result.failure()
              }
              Result.retry()
            }
          }

  private fun pooledResourcesChanged(data: MasterSyncResponse): Boolean {
    return data.profiles.isNotEmpty() || data.users.isNotEmpty() || data.bills.isNotEmpty()
  }
}
