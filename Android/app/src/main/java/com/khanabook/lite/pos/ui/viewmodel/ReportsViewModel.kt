package com.khanabook.lite.pos.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabook.lite.pos.data.repository.BillRepository
import com.khanabook.lite.pos.domain.manager.ReportGenerator
import com.khanabook.lite.pos.domain.model.OrderDetailRow
import com.khanabook.lite.pos.domain.model.OrderLevelRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    private val reportGenerator = ReportGenerator(billRepository)

    private val _paymentBreakdown = MutableStateFlow<Map<String, Double>>(emptyMap())
    val paymentBreakdown: StateFlow<Map<String, Double>> = _paymentBreakdown

    private val _orderLevelRows = MutableStateFlow<List<OrderLevelRow>>(emptyList())
    val orderLevelRows: StateFlow<List<OrderLevelRow>> = _orderLevelRows

    private val _orderDetailsTable = MutableStateFlow<List<OrderDetailRow>>(emptyList())
    val orderDetailsTable: StateFlow<List<OrderDetailRow>> = _orderDetailsTable

    private val _reportType = MutableStateFlow("Payment") // "Payment" or "Order"
    val reportType: StateFlow<String> = _reportType

    private val _timeFilter = MutableStateFlow("Daily") // "Daily", "Weekly", "Monthly", "Custom"
    val timeFilter: StateFlow<String> = _timeFilter

    private val _selectedBillDetails = MutableStateFlow<com.khanabook.lite.pos.data.local.relation.BillWithItems?>(null)
    val selectedBillDetails: StateFlow<com.khanabook.lite.pos.data.local.relation.BillWithItems?> = _selectedBillDetails

    private var currentFrom: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) + " 00:00:00"
    private var currentTo: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) + " 23:59:59"

    fun setReportType(type: String) {
        _reportType.value = type
    }

    fun setTimeFilter(filter: String) {
        _timeFilter.value = filter
        updateDateRangeAndLoad(filter)
    }

    fun loadBillDetails(billId: Int) {
        viewModelScope.launch {
            _selectedBillDetails.value = reportGenerator.getOrderDetail(billId)
        }
    }

    fun clearBillDetails() {
        _selectedBillDetails.value = null
    }

    private fun updateDateRangeAndLoad(filter: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // End of the range is always end of the current day
        val toDate = sdf.format(calendar.time)
        val to = "$toDate 23:59:59"

        val fromDate = when (filter) {
            "Daily" -> toDate
            "Weekly" -> {
                // Show last 7 days including today
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                sdf.format(calendar.time)
            }
            "Monthly" -> {
                // Set to first day of the current month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                sdf.format(calendar.time)
            }
            else -> toDate
        }
        
        val from = "$fromDate 00:00:00"
        loadReports(from, to)
    }

    fun setCustomDateRange(from: String, to: String) {
        _timeFilter.value = "Custom"
        // Ensure format is yyyy-MM-dd HH:mm:ss
        val formattedFrom = if (from.length <= 10) "$from 00:00:00" else from
        val formattedTo = if (to.length <= 10) "$to 23:59:59" else to
        loadReports(formattedFrom, formattedTo)
    }

    fun loadReports(from: String, to: String) {
        currentFrom = from
        currentTo = to
        viewModelScope.launch {
            _paymentBreakdown.value = reportGenerator.getPaymentBreakdown(from, to)
            _orderLevelRows.value = reportGenerator.getOrderLevelRows(from, to)
            _orderDetailsTable.value = reportGenerator.getOrderDetailsTable(from, to)
        }
    }

    fun updateOrderStatus(billId: Int, newStatus: String) {
        viewModelScope.launch {
            billRepository.updateOrderStatus(billId, newStatus)
            if (currentFrom.isNotEmpty() && currentTo.isNotEmpty()) {
                loadReports(currentFrom, currentTo)
            }
        }
    }

    fun updatePaymentMode(billId: Int, newMode: String) {
        viewModelScope.launch {
            billRepository.updatePaymentMode(billId, newMode)
            if (currentFrom.isNotEmpty() && currentTo.isNotEmpty()) {
                loadReports(currentFrom, currentTo)
            }
        }
    }
}


