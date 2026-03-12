package com.khanabook.lite.pos

import com.khanabook.lite.pos.data.local.entity.BillItemEntity
import com.khanabook.lite.pos.data.local.entity.RecipeIngredientEntity
import com.khanabook.lite.pos.data.repository.BatchRepository
import com.khanabook.lite.pos.data.repository.RecipeRepository
import com.khanabook.lite.pos.domain.manager.InventoryConsumptionManager
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InventoryConsumptionTest {

    @Mock
    private lateinit var recipeRepository: RecipeRepository

    @Mock
    private lateinit var batchRepository: BatchRepository

    private lateinit var inventoryConsumptionManager: InventoryConsumptionManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        inventoryConsumptionManager = InventoryConsumptionManager(recipeRepository, batchRepository)
    }

    @Test
    fun `consumeMaterialsForBill should calculate and consume correct quantities`() = runTest {
        // Arrange
        val menuItemId = 101
        val billItems = listOf(
            BillItemEntity(id = 1, billId = 1, menuItemId = menuItemId, itemName = "Biryani", quantity = 3, price = 200.0, itemTotal = 600.0)
        )

        val ingredients = listOf(
            RecipeIngredientEntity(id = 1, menuItemId = menuItemId, rawMaterialId = 501, quantityNeeded = 0.2) // 0.2kg Chicken per Biryani
        )

        whenever(recipeRepository.getIngredientsOnce(menuItemId)).thenReturn(ingredients)

        // Act
        inventoryConsumptionManager.consumeMaterialsForBill(billItems)

        // Assert: 3 Biryani * 0.2kg = 0.6kg
        val materialIdCaptor = argumentCaptor<Int>()
        val quantityCaptor = argumentCaptor<Double>()
        
        verify(batchRepository).consumeFromBatches(
            materialId = materialIdCaptor.capture(),
            totalToConsume = quantityCaptor.capture(),
            reason = any()
        )
        
        org.junit.Assert.assertEquals(501, materialIdCaptor.firstValue)
        org.junit.Assert.assertEquals(0.6, quantityCaptor.firstValue, 0.001)
    }
    
    @Test
    fun `consumeMaterialsForBill should handle multiple ingredients`() = runTest {
        // Arrange
        val menuItemId = 101
        val billItems = listOf(
            BillItemEntity(id = 1, billId = 1, menuItemId = menuItemId, itemName = "Biryani", quantity = 2, price = 200.0, itemTotal = 400.0)
        )

        val ingredients = listOf(
            RecipeIngredientEntity(id = 1, menuItemId = menuItemId, rawMaterialId = 501, quantityNeeded = 0.2), // Chicken
            RecipeIngredientEntity(id = 2, menuItemId = menuItemId, rawMaterialId = 502, quantityNeeded = 0.1)  // Rice
        )

        whenever(recipeRepository.getIngredientsOnce(menuItemId)).thenReturn(ingredients)

        // Act
        inventoryConsumptionManager.consumeMaterialsForBill(billItems)

        // Assert
        verify(batchRepository).consumeFromBatches(eq(501), eq(0.4), any())
        verify(batchRepository).consumeFromBatches(eq(502), eq(0.2), any())
    }
}
