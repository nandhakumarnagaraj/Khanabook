package com.khanabook.lite.pos.di

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.room.Room
import com.khanabook.lite.pos.BuildConfig
import com.khanabook.lite.pos.data.local.AppDatabase
import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.inject.Singleton
import net.sqlcipher.database.SupportFactory

private const val TAG = "DatabaseModule"
private const val KEYSTORE_ALIAS = "KhanaBookDbKey"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

        private fun getOrCreateDbPassphrase(): ByteArray {
                val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }

                if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                        val keyGen =
                                KeyGenerator.getInstance(
                                        KeyProperties.KEY_ALGORITHM_AES,
                                        "AndroidKeyStore"
                                )
                        keyGen.init(
                                KeyGenParameterSpec.Builder(
                                                KEYSTORE_ALIAS,
                                                KeyProperties.PURPOSE_ENCRYPT or
                                                        KeyProperties.PURPOSE_DECRYPT
                                        )
                                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                        .setEncryptionPaddings(
                                                KeyProperties.ENCRYPTION_PADDING_NONE
                                        )
                                        .setKeySize(256)
                                        .build()
                        )
                        keyGen.generateKey()
                }

                val alias = KEYSTORE_ALIAS.toByteArray(Charsets.UTF_8)
                return Base64.encode(alias, Base64.NO_WRAP)
        }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
                val passphrase = getOrCreateDbPassphrase()
                val factory = SupportFactory(passphrase)

                val builder =
                        Room.databaseBuilder(
                                        context,
                                        AppDatabase::class.java,
                                        AppDatabase.DATABASE_NAME
                                )
                                .openHelperFactory(factory)

                if (BuildConfig.DEBUG) {
                        builder.fallbackToDestructiveMigration()
                }

                return builder.build()
        }

        @Provides fun provideUserDao(database: AppDatabase) = database.userDao()
        @Provides fun provideRestaurantDao(database: AppDatabase) = database.restaurantDao()
        @Provides fun provideCategoryDao(database: AppDatabase) = database.categoryDao()
        @Provides fun provideMenuDao(database: AppDatabase) = database.menuDao()
        @Provides fun provideBillDao(database: AppDatabase) = database.billDao()
        @Provides fun provideInventoryDao(database: AppDatabase) = database.inventoryDao()

        @Provides
        @Singleton
        fun provideUserRepository(
                userDao: UserDao,
                @ApplicationContext context: Context,
                sessionManager: com.khanabook.lite.pos.domain.manager.SessionManager,
                workManager: androidx.work.WorkManager,
                api: com.khanabook.lite.pos.data.remote.api.KhanaBookApi
        ) =
                UserRepository(
                        userDao,
                        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE),
                        sessionManager,
                        workManager,
                        api
                )

        @Provides
        @Singleton
        fun provideRestaurantRepository(
                restaurantDao: RestaurantDao,
                sessionManager: com.khanabook.lite.pos.domain.manager.SessionManager,
                workManager: androidx.work.WorkManager,
                api: com.khanabook.lite.pos.data.remote.api.KhanaBookApi
        ) = RestaurantRepository(restaurantDao, sessionManager, workManager, api)

        @Provides
        @Singleton
        fun provideCategoryRepository(
                categoryDao: CategoryDao,
                sessionManager: com.khanabook.lite.pos.domain.manager.SessionManager,
                workManager: androidx.work.WorkManager
        ) = CategoryRepository(categoryDao, sessionManager, workManager)

        @Provides
        @Singleton
        fun provideMenuRepository(
                menuDao: MenuDao,
                sessionManager: com.khanabook.lite.pos.domain.manager.SessionManager,
                workManager: androidx.work.WorkManager
        ) = MenuRepository(menuDao, sessionManager, workManager)

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): androidx.work.WorkManager =
                androidx.work.WorkManager.getInstance(context)

        @Provides
        @Singleton
        fun provideBillRepository(
                billDao: BillDao,
                inventoryConsumptionManager:
                        com.khanabook.lite.pos.domain.manager.InventoryConsumptionManager,
                workManager: androidx.work.WorkManager
        ) = BillRepository(billDao, inventoryConsumptionManager, workManager)

        @Provides
        @Singleton
        fun provideInventoryRepository(
                inventoryDao: InventoryDao,
                menuDao: MenuDao,
                sessionManager: com.khanabook.lite.pos.domain.manager.SessionManager,
                workManager: androidx.work.WorkManager
        ) = InventoryRepository(inventoryDao, menuDao, sessionManager, workManager)

        @Provides
        @Singleton
        fun provideInventoryConsumptionManager(
                menuRepository: MenuRepository,
                inventoryRepository: InventoryRepository
        ) =
                com.khanabook.lite.pos.domain.manager.InventoryConsumptionManager(
                        menuRepository,
                        inventoryRepository
                )

        @Provides
        @Singleton
        fun provideBluetoothPrinterManager(@ApplicationContext context: Context) =
                com.khanabook.lite.pos.domain.manager.BluetoothPrinterManager(context)
}
