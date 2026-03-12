package com.khanabook.lite.pos.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabook.lite.pos.data.local.entity.BillEntity
import com.khanabook.lite.pos.data.repository.BillRepository
import com.khanabook.lite.pos.domain.manager.OrderIdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val networkMonitor: com.khanabook.lite.pos.domain.util.NetworkMonitor
) : ViewModel() {

    private val today = OrderIdManager.getTodayString()
    private val startOfDay = "$today 00:00:00"
    private val endOfDay = "$today 23:59:59"

    val connectionStatus: StateFlow<com.khanabook.lite.pos.domain.util.ConnectionStatus> = networkMonitor.status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.khanabook.lite.pos.domain.util.ConnectionStatus.Unavailable
        )

    val unsyncedCount: StateFlow<Int> = billRepository.getUnsyncedCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val todayStats: StateFlow<HomeStats> = billRepository.getBillsByDateRange(startOfDay, endOfDay)
        .map { bills ->
            val completedBills = bills.filter { it.paymentStatus == "success" }
            HomeStats(
                orderCount = completedBills.size,
                revenue = completedBills.sumOf { it.totalAmount },
                customerCount = completedBills.mapNotNull { it.customerWhatsapp }.distinct().size
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeStats()
        )


    data class HomeStats(
        val orderCount: Int = 0,
        val revenue: Double = 0.0,
        val customerCount: Int = 0
    )
}


