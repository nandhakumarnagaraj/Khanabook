package com.khanabook.lite.pos.data.local

import com.khanabook.lite.pos.data.local.dao.*
import com.khanabook.lite.pos.data.local.entity.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val categoryDao: CategoryDao,
    private val menuDao: MenuDao,
    private val userDao: UserDao,
    private val restaurantDao: RestaurantDao
) {

    suspend fun initialize() {
        if (userDao.getAllUsersOnce().isEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fullTimestamp = sdf.format(Date())

            // 1. Create Default Owner User
            userDao.insertUser(
                UserEntity(
                    name = "Owner",
                    email = "owner@khanabook.com",
                    passwordHash = "password",
                    isActive = true,
                    createdAt = fullTimestamp
                )
            )

            // 2. Create Restaurant Profile
            restaurantDao.saveProfile(
                RestaurantProfileEntity(
                    id = 1,
                    shopName = "KhanaBook Test Restaurant",
                    shopAddress = "123 Food Street",
                    whatsappNumber = "9876543210",
                    gstEnabled = true,
                    gstPercentage = 5.0
                )
            )

            // 3. Create Categories
            val startersId = categoryDao.insertCategory(CategoryEntity(name = "Starters", isVeg = true, createdAt = fullTimestamp)).toInt()
            val mainCourseId = categoryDao.insertCategory(CategoryEntity(name = "Main Course", isVeg = true, createdAt = fullTimestamp)).toInt()
            val beveragesId = categoryDao.insertCategory(CategoryEntity(name = "Beverages", isVeg = true, createdAt = fullTimestamp)).toInt()
            val dessertsId = categoryDao.insertCategory(CategoryEntity(name = "Desserts", isVeg = true, createdAt = fullTimestamp)).toInt()

            // 4. Create Menu Items
            val lollipopId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = startersId,
                    name = "Chicken Lollipop",
                    basePrice = 250.0,
                    foodType = "non-veg",
                    description = "Crispy chicken wings served with schezwan sauce.",
                    currentStock = 50.0,
                    lowStockThreshold = 10.0,
                    createdAt = fullTimestamp
                )
            ).toInt()

            val paneerTikkaId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = startersId,
                    name = "Paneer Tikka",
                    basePrice = 200.0,
                    foodType = "veg",
                    description = "Grilled cottage cheese cubes with Indian spices.",
                    currentStock = 30.0,
                    lowStockThreshold = 5.0,
                    createdAt = fullTimestamp
                )
            ).toInt()

            val pizzaId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = mainCourseId,
                    name = "Farmhouse Pizza",
                    basePrice = 0.0, // Use variants
                    foodType = "veg",
                    description = "Fresh veggies and mozzarella cheese.",
                    createdAt = fullTimestamp
                )
            ).toInt()

            val biryaniId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = mainCourseId,
                    name = "Chicken Biryani",
                    basePrice = 350.0,
                    foodType = "non-veg",
                    description = "Authentic Hyderabadi Dum Biryani.",
                    currentStock = 40.0,
                    lowStockThreshold = 5.0,
                    createdAt = fullTimestamp
                )
            ).toInt()

            val springRollId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = startersId,
                    name = "Veg Spring Rolls",
                    basePrice = 150.0,
                    foodType = "veg",
                    description = "Crispy rolls filled with sautéed vegetables.",
                    currentStock = 25.0,
                    lowStockThreshold = 5.0,
                    createdAt = fullTimestamp
                )
            ).toInt()

            val coffeeId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = beveragesId,
                    name = "Hot Coffee",
                    basePrice = 60.0,
                    foodType = "veg",
                    description = "Freshly brewed milk coffee.",
                    createdAt = fullTimestamp
                )
            ).toInt()

            val sodaId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = beveragesId,
                    name = "Fresh Lime Soda",
                    basePrice = 80.0,
                    foodType = "veg",
                    description = "Refreshing sweet and salty lime soda.",
                    createdAt = fullTimestamp
                )
            ).toInt()

            val jamunId = menuDao.insertItem(
                MenuItemEntity(
                    categoryId = dessertsId,
                    name = "Gulab Jamun (2pcs)",
                    basePrice = 100.0,
                    foodType = "veg",
                    description = "Soft milk-based dumplings in sugar syrup.",
                    currentStock = 20.0,
                    lowStockThreshold = 5.0,
                    createdAt = fullTimestamp
                )
            ).toInt()

            // 5. Create Item Variants for Pizza
            menuDao.insertVariant(ItemVariantEntity(menuItemId = pizzaId, variantName = "Regular", price = 299.0, currentStock = 20.0))
            menuDao.insertVariant(ItemVariantEntity(menuItemId = pizzaId, variantName = "Medium", price = 499.0, currentStock = 15.0))
            menuDao.insertVariant(ItemVariantEntity(menuItemId = pizzaId, variantName = "Large", price = 699.0, currentStock = 10.0))
        }
    }
}
