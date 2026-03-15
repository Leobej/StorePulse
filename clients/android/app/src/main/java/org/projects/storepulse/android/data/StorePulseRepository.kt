package org.projects.storepulse.android.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StorePulseRepository(
    private val baseUrl: String
) : StorePulseGateway {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    override suspend fun login(username: String, password: String): AuthResponse =
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }.body()

    override suspend fun currentSession(token: String): CurrentSessionResponse =
        client.get("$baseUrl/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    override suspend fun dashboard(token: String): DashboardOverviewResponse =
        client.get("$baseUrl/dashboard/overview") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    override suspend fun alerts(token: String): List<AlertResponse> =
        client.get("$baseUrl/alerts") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    override suspend fun acknowledgeAlert(token: String, alertId: String): AlertResponse =
        client.post("$baseUrl/alerts/$alertId/acknowledge") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    override suspend fun imports(token: String): List<ImportBatchResponse> =
        client.get("$baseUrl/imports") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

    override suspend fun switchStore(token: String, storeId: String): CurrentSessionResponse =
        client.post("$baseUrl/auth/switch-store") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(SwitchStoreRequest(storeId))
        }.body()

    override suspend fun uploadSalesImport(
        token: String,
        fileName: String,
        bytes: ByteArray
    ): ImportBatchResponse =
        client.post("$baseUrl/imports/sales") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                                append(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                            }
                        )
                    }
                )
            )
        }.body()
}
