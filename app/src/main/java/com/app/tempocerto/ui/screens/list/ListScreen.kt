package com.app.tempocerto.ui.screens.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.app.tempocerto.R
import com.app.tempocerto.ui.components.AppBottomBar
import com.app.tempocerto.ui.components.BlobBackground
import com.app.tempocerto.ui.components.ParameterChoiceDialog
import com.app.tempocerto.ui.components.SearchModalBottomSheet
import com.app.tempocerto.util.roboto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavHostController,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showParameterDialog by remember { mutableStateOf(false) }
    var showSearchModal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        BlobBackground()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.app_title),
                            fontFamily = roboto,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    actions = {
                        IconButton(onClick = { showParameterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Selecionar Parâmetro"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                AppBottomBar(
                    navController = navController,
                    onSearchClicked = { showSearchModal = true }
                )
            },
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ListActualParameters(
                    data = uiState.lastLogData,
                    onRefresh = viewModel::refresh,
                    lastLogDate = uiState.lastAvailableDate
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 24.dp)) {
                        ListParameter(
                            title = uiState.listTitle,
                            date = uiState.selectedDate,
                            logs = uiState.dailyLogs,
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            isNextDayEnabled = uiState.canNavigateForward,
                            adjustDate = { days -> viewModel.selectDate(uiState.selectedDate.plusDays(days)) }
                        )
                    }
                }
            }
        }

        if (showSearchModal) {
            SearchModalBottomSheet(
                onDismiss = { showSearchModal = false },
                onDateSelected = { date ->
                    val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    viewModel.selectDate(parsedDate)
                }
            )
        }

        if (showParameterDialog) {
            ParameterChoiceDialog(
                title = "Selecionar Parâmetro",
                parameters = uiState.availableParameters,
                onDismiss = { showParameterDialog = false },
                onParameterSelected = { selectedEnum ->
                    viewModel.selectParameter(selectedEnum)
                    showParameterDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListActualParameters(
    data: Map<String, String>,
    onRefresh: () -> Unit,
    lastLogDate: LocalDate?
) {
    var isRefreshing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 15.dp)) {
        Text(
            text = "Dados Atuais",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = roboto,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Em ${lastLogDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "..."}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = {
            isRefreshing = true
            onRefresh()
            isRefreshing = false
        }) {
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    items(data.entries.toList()) { entry ->
                        ModernDataCard(title = entry.key, value = entry.value)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDataCard(title: String, value: String) {
    ElevatedCard(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ListParameter(
    title: String,
    date: LocalDate,
    logs: List<Pair<String, String>>,
    isLoading: Boolean,
    error: String?,
    isNextDayEnabled: Boolean,
    adjustDate: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(title, fontSize = 22.sp, fontFamily = roboto, fontWeight = FontWeight.Medium)

        DateNavigationHeader(
            date = date,
            isNextDayEnabled = isNextDayEnabled,
            adjustDate = adjustDate
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error) }
            logs.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum dado encontrado para este dia.") }
            else -> {
                LazyColumn {
                    items(logs) { logPair ->
                        ListItem(
                            headlineContent = { Text(logPair.first, fontWeight = FontWeight.SemiBold) },
                            trailingContent = { Text(logPair.second, color = Color.Gray) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DateNavigationHeader(
    date: LocalDate,
    isNextDayEnabled: Boolean,
    adjustDate: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { adjustDate(-1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Dia anterior")
        }

        Text(
            text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        IconButton(onClick = { adjustDate(1) }, enabled = isNextDayEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Dia posterior",
                tint = if (isNextDayEnabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
            )
        }
    }
}
