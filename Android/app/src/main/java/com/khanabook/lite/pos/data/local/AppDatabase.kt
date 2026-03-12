package com.khanabook.lite.pos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.local.entity.*

@Database(
        entities =
                [
                        UserEntity::class,
                        RestaurantProfileEntity::class,
                        CategoryEntity::class,
                        MenuItemEntity::class,
                        ItemVariantEntity::class,
                        BillEntity::class,
                        BillItemEntity::class,
                        BillPaymentEntity::class,
                        StockLogEntity::class
                ],
        version = 18,
        exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun categoryDao(): CategoryDao
    abstract fun menuDao(): MenuDao
    abstract fun billDao(): BillDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        const val DATABASE_NAME = "khanabook_lite_db"

        // Simplified: Incremented version to 18. 
        // In a production app, we would write migrations to:
        // 1. Drop raw_materials, material_batches, recipe_ingredients, raw_material_stock_logs
        // 2. Add current_stock and low_stock_threshold to menu_items and item_variants
        // 3. Remove role from users
    }
}
