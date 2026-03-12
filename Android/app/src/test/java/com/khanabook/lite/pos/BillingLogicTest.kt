package com.khanabook.lite.pos

import org.junit.Test
import org.junit.Assert.*

class BillingLogicTest {

    @Test
    fun testAddToCartStockLogic() {
        val itemName = "Burger"
        val stockQuantity = 5
        val lowStockThreshold = 2
        
        var cartQuantity = 0
        var errorMessage: String? = null
        
        fun addToCartSim(currentQty: Int) {
            if (currentQty >= stockQuantity) {
                errorMessage = "Reached maximum stock for $itemName"
                return
            }
            
            val remainingAfterAdd = stockQuantity - (currentQty + 1)
            if (remainingAfterAdd <= lowStockThreshold && remainingAfterAdd > 0) {
                errorMessage = "Running out of stock for $itemName"
            } else if (remainingAfterAdd == 0) {
                errorMessage = "Reached maximum stock for $itemName"
            } else {
                errorMessage = null
            }
            cartQuantity = currentQty + 1
        }
        
        // Add 1st (5-1=4 > 2, no error)
        addToCartSim(0)
        assertNull("Error at 1: $errorMessage", errorMessage)
        
        // Add 2nd (5-2=3 > 2, no error)
        addToCartSim(1)
        assertNull("Error at 2: $errorMessage", errorMessage)
        
        // Add 3rd (5-3=2 <= 2, "Running out of stock")
        addToCartSim(2)
        assertEquals("Running out of stock for Burger", errorMessage)
        
        // Add 4th (5-4=1 <= 2, "Running out of stock")
        addToCartSim(3)
        assertEquals("Running out of stock for Burger", errorMessage)
        
        // Add 5th (5-5=0, "Reached maximum stock")
        addToCartSim(4)
        assertEquals("Reached maximum stock for Burger", errorMessage)
        
        // Add 6th (Attempts to add when cart is 5, "Reached maximum stock" and return)
        addToCartSim(5)
        assertEquals("Reached maximum stock for Burger", errorMessage)
    }
}


