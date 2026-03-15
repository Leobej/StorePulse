package org.projects.storepulse.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.projects.storepulse.android.data.AlertResponse
import org.projects.storepulse.android.data.CurrentSessionResponse
import org.projects.storepulse.android.data.DashboardOverviewResponse
import org.projects.storepulse.android.data.ImportBatchResponse
import org.projects.storepulse.android.data.SessionStore
import org.projects.storepulse.android.data.StorePulseGateway

data class StorePulseUiState(
    val initialized: Boolean = false,
    val loading: Boolean = false,
    val uploadInFlight: Boolean = false,
    val token: String? = null,
    val username: String? = null,
    val session: CurrentSessionResponse? = null,
    val dashboard: DashboardOverviewResponse? = null,
    val alerts: List<AlertResponse> = emptyList(),
    val imports: List<ImportBatchResponse> = emptyList(),
    val error: String? = null
)

class StorePulseViewModel(
    private val repository: StorePulseGateway,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(StorePulseUiState())
    val uiState: StateFlow<StorePulseUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val token = sessionStore.accessToken().first()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(initialized = true)
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                initialized = true,
                loading = true,
                token = token,
                error = null
            )
            refresh(token, clearOnFailure = true)
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(initialized = true, loading = true, error = null)
            runCatching { repository.login(username, password) }
                .onSuccess { auth ->
                    sessionStore.saveAccessToken(auth.accessToken)
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        token = auth.accessToken,
                        username = auth.username,
                        session = CurrentSessionResponse(auth.username, auth.currentStore, auth.allowedStores)
                    )
                    refresh(auth.accessToken)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(loading = false, error = error.message)
                }
        }
    }

    fun refresh() {
        val token = _uiState.value.token ?: return
        refresh(token)
    }

    private fun refresh(
        token: String,
        clearOnFailure: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                val session = repository.currentSession(token)
                val dashboard = repository.dashboard(token)
                val alerts = repository.alerts(token)
                val imports = repository.imports(token)
                RefreshPayload(session, dashboard, alerts, imports)
            }.onSuccess { payload ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    session = payload.session,
                    username = payload.session.username,
                    dashboard = payload.dashboard,
                    alerts = payload.alerts,
                    imports = payload.imports
                )
            }.onFailure { error ->
                if (clearOnFailure && _uiState.value.token != null) {
                    clearSession(error.message)
                } else {
                    _uiState.value = _uiState.value.copy(loading = false, uploadInFlight = false, error = error.message)
                }
            }
        }
    }

    fun acknowledgeAlert(alertId: String) {
        val token = _uiState.value.token ?: return
        viewModelScope.launch {
            runCatching { repository.acknowledgeAlert(token, alertId) }
                .onSuccess { refresh() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun switchStore(storeId: String) {
        val token = _uiState.value.token ?: return
        viewModelScope.launch {
            runCatching { repository.switchStore(token, storeId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(session = it)
                    refresh()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            clearSession()
        }
    }

    fun uploadSalesImport(fileName: String, bytes: ByteArray) {
        val token = _uiState.value.token ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadInFlight = true, error = null)
            runCatching { repository.uploadSalesImport(token, fileName, bytes) }
                .onSuccess { batch ->
                    _uiState.value = _uiState.value.copy(
                        uploadInFlight = false,
                        imports = listOf(batch) + _uiState.value.imports
                    )
                    refresh()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(uploadInFlight = false, error = error.message)
                }
        }
    }

    private suspend fun clearSession(error: String? = null) {
        sessionStore.clear()
        _uiState.value = StorePulseUiState(initialized = true, error = error)
    }
}

private data class RefreshPayload(
    val session: CurrentSessionResponse,
    val dashboard: DashboardOverviewResponse,
    val alerts: List<AlertResponse>,
    val imports: List<ImportBatchResponse>
)
