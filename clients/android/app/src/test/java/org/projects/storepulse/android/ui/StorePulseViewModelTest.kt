package org.projects.storepulse.android.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.projects.storepulse.android.data.AlertResponse
import org.projects.storepulse.android.data.AuthResponse
import org.projects.storepulse.android.data.CurrentSessionResponse
import org.projects.storepulse.android.data.DashboardOverviewResponse
import org.projects.storepulse.android.data.DailyAggregateResponse
import org.projects.storepulse.android.data.ImportBatchResponse
import org.projects.storepulse.android.data.SessionStore
import org.projects.storepulse.android.data.StorePulseGateway
import org.projects.storepulse.android.data.StoreSummary

@OptIn(ExperimentalCoroutinesApi::class)
class StorePulseViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun restoresPersistedSessionOnStartup() = runTest {
        val gateway = FakeGateway()
        val sessionStore = FakeSessionStore("token-123")

        val viewModel = StorePulseViewModel(gateway, sessionStore)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.initialized)
        assertEquals("manager", state.username)
        assertEquals("Central", state.session?.currentStore?.name)
        assertEquals(1, state.imports.size)
    }

    @Test
    fun uploadsSalesImportAndRefreshesImports() = runTest {
        val gateway = FakeGateway()
        val sessionStore = FakeSessionStore("token-123")
        val viewModel = StorePulseViewModel(gateway, sessionStore)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadSalesImport("march.csv", "sku,qty".toByteArray())
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.uploadInFlight)
        assertEquals("march.csv", gateway.lastUploadName)
        assertEquals(2, state.imports.size)
    }

    @Test
    fun logoutClearsPersistedToken() = runTest {
        val gateway = FakeGateway()
        val sessionStore = FakeSessionStore("token-123")
        val viewModel = StorePulseViewModel(gateway, sessionStore)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.logout()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, sessionStore.accessToken().first())
        assertEquals(null, viewModel.uiState.value.token)
    }
}

private class FakeGateway : StorePulseGateway {
    var lastUploadName: String? = null

    override suspend fun login(username: String, password: String): AuthResponse =
        AuthResponse("token-123", "Bearer", 3600, "manager", store(), listOf(store()))

    override suspend fun currentSession(token: String): CurrentSessionResponse =
        CurrentSessionResponse("manager", store(), listOf(store()))

    override suspend fun dashboard(token: String): DashboardOverviewResponse =
        DashboardOverviewResponse(
            dailyAggregates = listOf(
                DailyAggregateResponse(store().id, "2026-03-15", 1200.0, 42, 18, 66.6, "Coffee", 9)
            ),
            openAlerts = listOf(alert())
        )

    override suspend fun alerts(token: String): List<AlertResponse> = listOf(alert())

    override suspend fun acknowledgeAlert(token: String, alertId: String): AlertResponse = alert().copy(status = "ACKNOWLEDGED")

    override suspend fun imports(token: String): List<ImportBatchResponse> =
        listOf(existingImport(), uploadedImport()).filterNot { it.originalFileName == "march.csv" && lastUploadName == null }

    override suspend fun switchStore(token: String, storeId: String): CurrentSessionResponse =
        CurrentSessionResponse("manager", store(), listOf(store()))

    override suspend fun uploadSalesImport(token: String, fileName: String, bytes: ByteArray): ImportBatchResponse {
        lastUploadName = fileName
        return uploadedImport().copy(originalFileName = fileName)
    }

    private fun store() = StoreSummary("store-1", "CENTRAL", "Central")

    private fun alert() = AlertResponse("alert-1", "store-1", "LOW_REVENUE", "HIGH", "OPEN", "2026-03-15", "Revenue dropped.")

    private fun existingImport() = ImportBatchResponse(
        id = "batch-1",
        storeId = "store-1",
        type = "SALES",
        status = "COMPLETED",
        originalFileName = "existing.csv",
        fileChecksum = "abc",
        processedRows = 10,
        successfulRows = 10,
        failedRows = 0,
        createdAt = "2026-03-15T10:00:00Z",
        updatedAt = "2026-03-15T10:01:00Z"
    )

    private fun uploadedImport() = ImportBatchResponse(
        id = "batch-2",
        storeId = "store-1",
        type = "SALES",
        status = "UPLOADED",
        originalFileName = "march.csv",
        fileChecksum = "def",
        processedRows = 0,
        successfulRows = 0,
        failedRows = 0,
        createdAt = "2026-03-15T11:00:00Z",
        updatedAt = null
    )
}

private class FakeSessionStore(initialToken: String?) : SessionStore {
    private val tokenFlow = MutableStateFlow(initialToken)

    override fun accessToken(): Flow<String?> = tokenFlow

    override suspend fun saveAccessToken(token: String) {
        tokenFlow.value = token
    }

    override suspend fun clear() {
        tokenFlow.value = null
    }
}
