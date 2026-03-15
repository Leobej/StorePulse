package org.projects.storepulse.android

import android.content.Context
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
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
import org.projects.storepulse.android.ui.StorePulseViewModel

class MainActivitySessionRestoreTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var fakeGateway: ActivityFakeGateway

    @Before
    fun setUp() {
        fakeGateway = ActivityFakeGateway()
        StorePulseAppGraph.factory = { _: Context ->
            StorePulseViewModel(fakeGateway, FakeSessionStore("persisted-token"))
        }
    }

    @After
    fun tearDown() {
        StorePulseAppGraph.reset()
    }

    @Test
    fun coldStartRestoresPersistedSessionAndShowsDashboard() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.onNodeWithTag("login_submit").assertDoesNotExist()
            composeRule.onNodeWithText("Central").assertIsDisplayed()
            composeRule.onNodeWithTag("dashboard_open_alerts").assertIsDisplayed()
            composeRule.onNodeWithText("2026-03-15: 1200.0 revenue, 42 units").assertIsDisplayed()
            assertEquals("persisted-token", fakeGateway.lastSessionToken)
        }
    }
}

private class ActivityFakeGateway : StorePulseGateway {
    var lastSessionToken: String? = null

    override suspend fun login(username: String, password: String): AuthResponse =
        AuthResponse("persisted-token", "Bearer", 3600, "manager", store(), listOf(store()))

    override suspend fun currentSession(token: String): CurrentSessionResponse {
        lastSessionToken = token
        return CurrentSessionResponse("manager", store(), listOf(store()))
    }

    override suspend fun dashboard(token: String): DashboardOverviewResponse =
        DashboardOverviewResponse(
            dailyAggregates = listOf(
                DailyAggregateResponse("store-1", "2026-03-15", 1200.0, 42, 18, 66.6, "Coffee", 9)
            ),
            openAlerts = listOf(
                AlertResponse("alert-1", "store-1", "LOW_REVENUE", "HIGH", "OPEN", "2026-03-15", "Revenue dropped.")
            )
        )

    override suspend fun alerts(token: String): List<AlertResponse> =
        listOf(AlertResponse("alert-1", "store-1", "LOW_REVENUE", "HIGH", "OPEN", "2026-03-15", "Revenue dropped."))

    override suspend fun acknowledgeAlert(token: String, alertId: String): AlertResponse =
        AlertResponse(alertId, "store-1", "LOW_REVENUE", "HIGH", "ACKNOWLEDGED", "2026-03-15", "Revenue dropped.")

    override suspend fun imports(token: String): List<ImportBatchResponse> =
        listOf(
            ImportBatchResponse(
                id = "batch-1",
                storeId = "store-1",
                type = "SALES",
                status = "COMPLETED",
                originalFileName = "sales-march.csv",
                fileChecksum = "abc",
                processedRows = 120,
                successfulRows = 120,
                failedRows = 0,
                createdAt = "2026-03-15T10:00:00Z",
                updatedAt = "2026-03-15T10:05:00Z"
            )
        )

    override suspend fun switchStore(token: String, storeId: String): CurrentSessionResponse =
        CurrentSessionResponse("manager", store(), listOf(store()))

    override suspend fun uploadSalesImport(token: String, fileName: String, bytes: ByteArray): ImportBatchResponse =
        ImportBatchResponse(
            id = "batch-uploaded",
            storeId = "store-1",
            type = "SALES",
            status = "UPLOADED",
            originalFileName = fileName,
            fileChecksum = "uploaded",
            processedRows = 0,
            successfulRows = 0,
            failedRows = 0,
            createdAt = "2026-03-15T12:00:00Z",
            updatedAt = null
        )

    private fun store() = StoreSummary("store-1", "CENTRAL", "Central")
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
