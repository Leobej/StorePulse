package org.projects.storepulse.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.projects.storepulse.android.data.AlertResponse
import org.projects.storepulse.android.data.CurrentSessionResponse
import org.projects.storepulse.android.data.DashboardOverviewResponse
import org.projects.storepulse.android.data.DailyAggregateResponse
import org.projects.storepulse.android.data.ImportBatchResponse
import org.projects.storepulse.android.data.StoreSummary

class StorePulseAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loginScreenSubmitsCredentials() {
        var submittedUsername: String? = null
        var submittedPassword: String? = null

        composeRule.setContent {
            StorePulseApp(
                state = StorePulseUiState(initialized = true),
                onLogin = { username, password ->
                    submittedUsername = username
                    submittedPassword = password
                },
                onRefresh = {},
                onLogout = {},
                onAcknowledge = {},
                onSwitchStore = {},
                onUploadImport = { _, _ -> }
            )
        }

        composeRule.onNodeWithTag("login_username").performTextReplacement("operator")
        composeRule.onNodeWithTag("login_password").performTextReplacement("secret-123")
        composeRule.onNodeWithTag("login_submit").performClick()

        assertEquals("operator", submittedUsername)
        assertEquals("secret-123", submittedPassword)
    }

    @Test
    fun authenticatedAppNavigatesToImportsAndShowsUploadAction() {
        composeRule.setContent {
            StorePulseApp(
                state = authenticatedState(),
                onLogin = { _, _ -> },
                onRefresh = {},
                onLogout = {},
                onAcknowledge = {},
                onSwitchStore = {},
                onUploadImport = { _, _ -> },
                onRequestImportUpload = { _ -> }
            )
        }

        composeRule.onNodeWithTag("nav_imports").performClick()
        composeRule.onNodeWithTag("imports_title").assertIsDisplayed()
        composeRule.onNodeWithTag("imports_upload_button").assertExists()
        composeRule.onNodeWithText("sales-march.csv").assertIsDisplayed()
    }

    @Test
    fun authenticatedAppNavigatesToAlertsAndShowsAlertContent() {
        composeRule.setContent {
            StorePulseApp(
                state = authenticatedState(),
                onLogin = { _, _ -> },
                onRefresh = {},
                onLogout = {},
                onAcknowledge = {},
                onSwitchStore = {},
                onUploadImport = { _, _ -> },
                onRequestImportUpload = { _ -> }
            )
        }

        composeRule.onNodeWithTag("nav_alerts").performClick()
        composeRule.onNodeWithText("Alerts").assertIsDisplayed()
        composeRule.onNodeWithText("LOW_REVENUE | HIGH").assertIsDisplayed()
        composeRule.onNodeWithText("Ack").assertExists()
    }

    @Test
    fun importsUploadButtonCanDriveFakePickerResultIntoUploadCallback() {
        var uploadedFileName: String? = null
        var uploadedBytes: ByteArray? = null

        composeRule.setContent {
            StorePulseApp(
                state = authenticatedState(),
                onLogin = { _, _ -> },
                onRefresh = {},
                onLogout = {},
                onAcknowledge = {},
                onSwitchStore = {},
                onUploadImport = { fileName, bytes ->
                    uploadedFileName = fileName
                    uploadedBytes = bytes
                },
                onRequestImportUpload = { onSelected ->
                    onSelected("picked-sales.csv", "sku,qty\ncoffee,2".toByteArray())
                }
            )
        }

        composeRule.onNodeWithTag("nav_imports").performClick()
        composeRule.onNodeWithTag("imports_upload_button").performClick()

        assertEquals("picked-sales.csv", uploadedFileName)
        assertEquals("sku,qty\ncoffee,2", uploadedBytes?.decodeToString())
    }
}

private fun authenticatedState(): StorePulseUiState {
    val store = StoreSummary("store-1", "CENTRAL", "Central")
    return StorePulseUiState(
        initialized = true,
        token = "token-123",
        username = "manager",
        session = CurrentSessionResponse("manager", store, listOf(store)),
        dashboard = DashboardOverviewResponse(
            dailyAggregates = listOf(
                DailyAggregateResponse("store-1", "2026-03-15", 1200.0, 42, 18, 66.6, "Coffee", 9)
            ),
            openAlerts = listOf(
                AlertResponse("alert-1", "store-1", "LOW_REVENUE", "HIGH", "OPEN", "2026-03-15", "Revenue dropped.")
            )
        ),
        alerts = listOf(
            AlertResponse("alert-1", "store-1", "LOW_REVENUE", "HIGH", "OPEN", "2026-03-15", "Revenue dropped.")
        ),
        imports = listOf(
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
    )
}
