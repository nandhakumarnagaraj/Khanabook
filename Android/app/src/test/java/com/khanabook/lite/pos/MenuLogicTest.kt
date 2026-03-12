package com.khanabook.lite.pos

import org.junit.Test
import org.junit.Assert.*

class MenuLogicTest {

    @Test
    fun testDuplicateCategoryCheck() {
        val existingCategories = listOf("Testing", "Food")
        
        fun isDuplicate(name: String): Boolean {
            return existingCategories.any { it.equals(name, ignoreCase = true) }
        }
        
        assertTrue("Should be duplicate", isDuplicate("testing"))
        assertTrue("Should be duplicate", isDuplicate("TESTING"))
        assertFalse("Should not be duplicate", isDuplicate("New Category"))
    }
}


