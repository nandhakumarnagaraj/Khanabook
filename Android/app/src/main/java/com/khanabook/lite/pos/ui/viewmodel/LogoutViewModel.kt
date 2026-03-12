package com.khanabook.lite.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khanabook.lite.pos.data.local.AppDatabase
import com.khanabook.lite.pos.data.repository.BillRepository
import com.khanabook.lite.pos.data.repository.UserRepository
import com.khanabook.lite.pos.domain.manager.SessionManager
import com.khanabook.lite.pos.domain.manager.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LogoutState {
    object Idle : LogoutState()
    object AttemptingPush : LogoutState()
    data class WarningOfflineData(val count: Int) : LogoutState()
    object LoggedOut : LogoutState()
}

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val appDatabase: AppDatabase,
    private val billRepository: BillRepository,
    private val syncManager: SyncManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    fun initiateLogout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.AttemptingPush

            try {
                // To safely get count without Flow, we can use appDatabase directly or if billRepository has a suspend fun.
                val unsyncedCount = appDatabase.billDao().getUnsyncedBills().size
                if (unsyncedCount > 0) {
                    val success = syncManager.pushUnsyncedDataImmediately()
                    if (success) {
                        performHardLogout()
                    } else {
                        _logoutState.value = LogoutState.WarningOfflineData(unsyncedCount)
                    }
                } else {
                    performHardLogout()
                }
            } catch (e: Exception) {
                // DB error?
                performHardLogout()
            }
        }
    }

    fun forceLogoutDespiteWarning() {
        performHardLogout()
    }

    fun cancelLogout() {
        _logoutState.value = LogoutState.Idle
    }

    private fun performHardLogout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            // clear tables but maybe keep some? The prompt says `clearAllTables()`
            appDatabase.clearAllTables()
            userRepository.setCurrentUser(null)
            _logoutState.value = LogoutState.LoggedOut
        }
    }
}
