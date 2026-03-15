package org.projects.storepulse.android.data

interface StorePulseGateway {
    suspend fun login(username: String, password: String): AuthResponse
    suspend fun currentSession(token: String): CurrentSessionResponse
    suspend fun dashboard(token: String): DashboardOverviewResponse
    suspend fun alerts(token: String): List<AlertResponse>
    suspend fun acknowledgeAlert(token: String, alertId: String): AlertResponse
    suspend fun imports(token: String): List<ImportBatchResponse>
    suspend fun switchStore(token: String, storeId: String): CurrentSessionResponse
    suspend fun uploadSalesImport(token: String, fileName: String, bytes: ByteArray): ImportBatchResponse
}
