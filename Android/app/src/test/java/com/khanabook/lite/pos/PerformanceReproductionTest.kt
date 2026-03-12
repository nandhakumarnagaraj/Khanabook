package com.khanabook.lite.pos

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Test

class PerformanceReproductionTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testUpdateSummaryDebouncing() = runTest {
        var summaryUpdateCount = 0
        val cartItems = MutableStateFlow<List<String>>(emptyList())

        // Simulate the BillingViewModel init block
        backgroundScope.launch {
            cartItems
                .debounce(300)
                .onEach { 
                    summaryUpdateCount++ 
                }
                .collect()
        }

        // Rapid updates
        cartItems.value = listOf("Item 1")
        advanceTimeBy(100)
        cartItems.value = listOf("Item 1", "Item 2")
        advanceTimeBy(100)
        cartItems.value = listOf("Item 1", "Item 2", "Item 3")
        
        // At this point, summaryUpdateCount should still be 0 because 300ms hasn't passed since the last update
        assertEquals(0, summaryUpdateCount)

        // Advance past the debounce period
        advanceTimeBy(301)
        
        // Now it should have been called once
        assertEquals(1, summaryUpdateCount)
        
        // Another update after a while
        advanceTimeBy(1000)
        cartItems.value = listOf("Item 1", "Item 2", "Item 3", "Item 4")
        advanceTimeBy(301)
        
        // Total should be 2
        assertEquals(2, summaryUpdateCount)
    }
}
