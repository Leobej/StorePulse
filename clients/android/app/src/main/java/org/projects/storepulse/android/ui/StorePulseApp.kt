package org.projects.storepulse.android.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private enum class StorePulseRoute(val route: String, val label: String) {
    Dashboard("dashboard", "Dashboard"),
    Alerts("alerts", "Alerts"),
    Imports("imports", "Imports")
}

@Composable
fun StorePulseApp(viewModel: StorePulseViewModel) {
    val state by viewModel.uiState.collectAsState()
    StorePulseApp(
        state = state,
        onLogin = viewModel::login,
        onRefresh = viewModel::refresh,
        onLogout = viewModel::logout,
        onAcknowledge = viewModel::acknowledgeAlert,
        onSwitchStore = viewModel::switchStore,
        onUploadImport = viewModel::uploadSalesImport,
        onRequestImportUpload = null
    )
}

@Composable
internal fun StorePulseApp(
    state: StorePulseUiState,
    onLogin: (String, String) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onAcknowledge: (String) -> Unit,
    onSwitchStore: (String) -> Unit,
    onUploadImport: (String, ByteArray) -> Unit,
    onRequestImportUpload: (((String, ByteArray) -> Unit) -> Unit)? = null
) {
    MaterialTheme {
        when {
            !state.initialized -> LoadingScreen()
            state.token == null -> {
                LoginScreen(
                    state = state,
                    onLogin = onLogin
                )
            }
            else -> AuthenticatedApp(
                state = state,
                onRefresh = onRefresh,
                onLogout = onLogout,
                onAcknowledge = onAcknowledge,
                onSwitchStore = onSwitchStore,
                onUploadImport = onUploadImport,
                onRequestImportUpload = onRequestImportUpload
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoginScreen(
    state: StorePulseUiState,
    onLogin: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("storepulse-admin") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("StorePulse Android", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("login_title"))
        OutlinedTextField(
            modifier = Modifier.testTag("login_username"),
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        OutlinedTextField(
            modifier = Modifier.testTag("login_password"),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Button(
            modifier = Modifier.testTag("login_submit"),
            onClick = { onLogin(username, password) },
            enabled = !state.loading
        ) {
            Text(if (state.loading) "Signing in..." else "Sign In")
        }
        if (state.error != null) {
            Text(state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("login_error"))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedApp(
    state: StorePulseUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onAcknowledge: (String) -> Unit,
    onSwitchStore: (String) -> Unit,
    onUploadImport: (String, ByteArray) -> Unit,
    onRequestImportUpload: (((String, ByteArray) -> Unit) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val session = state.session
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val routes = StorePulseRoute.entries

    LaunchedEffect(state.token) {
        navController.navigate(StorePulseRoute.Dashboard.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.currentStore?.name ?: "StorePulse") },
                actions = {
                    Button(modifier = Modifier.testTag("refresh_button"), onClick = onRefresh) { Text("Refresh") }
                    Button(modifier = Modifier.testTag("logout_button"), onClick = onLogout) { Text("Logout") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                routes.forEach { route ->
                    val selected = currentDestination?.hierarchy?.any { it.route == route.route } == true
                    NavigationBarItem(
                        modifier = Modifier.testTag("nav_${route.route}"),
                        selected = selected,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {},
                        label = { Text(route.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = StorePulseRoute.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(StorePulseRoute.Dashboard.route) {
                DashboardScreen(
                    state = state,
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    onDismissStoreMenu = { expanded = false },
                    onSwitchStore = { storeId ->
                        expanded = false
                        onSwitchStore(storeId)
                    }
                )
            }
            composable(StorePulseRoute.Alerts.route) {
                AlertsScreen(state = state, onAcknowledge = onAcknowledge)
            }
            composable(StorePulseRoute.Imports.route) {
                ImportsRoute(
                    state = state,
                    onUploadImport = onUploadImport,
                    onRequestImportUpload = onRequestImportUpload
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    state: StorePulseUiState,
    expanded: Boolean,
    onExpandedChange: () -> Unit,
    onDismissStoreMenu: () -> Unit,
    onSwitchStore: (String) -> Unit
) {
    val session = state.session

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Session", style = MaterialTheme.typography.titleMedium)
                    Text("User: ${state.username}")
                    Text("Store: ${session?.currentStore?.code ?: "-"}")
                    if ((session?.allowedStores?.size ?: 0) > 1) {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandedChange() }) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                value = session?.currentStore?.name.orEmpty(),
                                onValueChange = {},
                                label = { Text("Switch store") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = onDismissStoreMenu) {
                                session?.allowedStores?.forEach { store ->
                                    DropdownMenuItem(
                                        text = { Text(store.name) },
                                        onClick = { onSwitchStore(store.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily Aggregates", style = MaterialTheme.typography.titleMedium)
                    if (state.dashboard?.dailyAggregates.isNullOrEmpty()) {
                        Text("No dashboard data yet.")
                    } else {
                        state.dashboard?.dailyAggregates?.forEach { aggregate ->
                            Text("${aggregate.businessDate}: ${aggregate.revenue} revenue, ${aggregate.unitsSold} units")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Open Alerts", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${state.alerts.count { it.status == "OPEN" }} active alerts",
                        modifier = Modifier.testTag("dashboard_open_alerts")
                    )
                }
            }
        }

        if (state.error != null) {
            item {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AlertsScreen(
    state: StorePulseUiState,
    onAcknowledge: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Alerts", style = MaterialTheme.typography.titleLarge)
        }
        if (state.alerts.isEmpty()) {
            item {
                Text("No alerts.")
            }
        } else {
            items(state.alerts, key = { it.id }) { alert ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${alert.type} | ${alert.severity}")
                            Text(alert.businessDate, style = MaterialTheme.typography.bodySmall)
                            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (alert.status == "OPEN") {
                            Button(onClick = { onAcknowledge(alert.id) }) {
                                Text("Ack")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportsRoute(
    state: StorePulseUiState,
    onUploadImport: (String, ByteArray) -> Unit,
    onRequestImportUpload: (((String, ByteArray) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val upload = uri?.toUploadCandidate(context) ?: return@rememberLauncherForActivityResult
        onUploadImport(upload.fileName, upload.bytes)
    }
    val requestImportUpload = onRequestImportUpload ?: { onSelected: (String, ByteArray) -> Unit ->
        importPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
    }

    ImportsScreen(
        state = state,
        onRequestImportUpload = {
            requestImportUpload(onUploadImport)
        }
    )
}

@Composable
private fun ImportsScreen(
    state: StorePulseUiState,
    onRequestImportUpload: () -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Imports", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("imports_title"))
                Button(
                    modifier = Modifier.testTag("imports_upload_button"),
                    onClick = onRequestImportUpload,
                    enabled = !state.uploadInFlight
                ) {
                    Text(if (state.uploadInFlight) "Uploading..." else "Upload CSV")
                }
            }
        }
        if (state.imports.isEmpty()) {
            item {
                Text("No imports for this store.")
            }
        } else {
            items(state.imports, key = { it.id }) { batch ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(batch.originalFileName, style = MaterialTheme.typography.titleMedium)
                        Text("Status: ${batch.status}")
                        Text("Processed: ${batch.processedRows} rows")
                        if (batch.errorMessage != null) {
                            Text(batch.errorMessage, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        if (state.error != null) {
            item {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private data class UploadCandidate(
    val fileName: String,
    val bytes: ByteArray
)

private fun Uri.toUploadCandidate(context: Context): UploadCandidate? {
    val fileName = context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameColumn >= 0) cursor.getString(nameColumn) else null
    } ?: "sales-import.csv"

    val bytes = context.contentResolver.openInputStream(this)?.use { input -> input.readBytes() } ?: return null
    return UploadCandidate(fileName, bytes)
}
