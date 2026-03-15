package org.projects.storepulse.android.data

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val username: String,
    val currentStore: StoreSummary,
    val allowedStores: List<StoreSummary>
)

@Serializable
data class StoreSummary(
    val id: String,
    val code: String,
    val name: String
)

@Serializable
data class CurrentSessionResponse(
    val username: String,
    val currentStore: StoreSummary,
    val allowedStores: List<StoreSummary>
)

@Serializable
data class DashboardOverviewResponse(
    val dailyAggregates: List<DailyAggregateResponse>,
    val openAlerts: List<AlertResponse>
)

@Serializable
data class DailyAggregateResponse(
    val storeId: String,
    val businessDate: String,
    val revenue: Double,
    val unitsSold: Int,
    val receipts: Int,
    val averageBasket: Double,
    val topProductName: String? = null,
    val topProductUnits: Int? = null
)

@Serializable
data class AlertResponse(
    val id: String,
    val storeId: String,
    val type: String,
    val severity: String,
    val status: String,
    val businessDate: String,
    val message: String
)

@Serializable
data class ImportBatchResponse(
    val id: String,
    val storeId: String,
    val type: String,
    val status: String,
    val originalFileName: String,
    val fileChecksum: String,
    val processedRows: Int,
    val successfulRows: Int,
    val failedRows: Int,
    val errorMessage: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class SwitchStoreRequest(
    val storeId: String
)
