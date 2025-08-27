package com.app.tempocerto.ui.screens.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.app.tempocerto.R
import com.app.tempocerto.ui.components.BottomBar
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_title),
                        fontFamily = roboto,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                contentPadding = PaddingValues(0.dp)
            ) {
                BottomBar(
                    navController = navController,
                    onSearchClicked = { showSearchModal = true }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ListActualParameters(
                data = uiState.lastLogData,
                onRefresh = viewModel::refresh,
                lastLogDate = uiState.lastAvailableDate
            )

            Spacer(Modifier.height(24.dp))

            ListParameter(
                title = uiState.listTitle,
                date = uiState.selectedDate,
                logs = uiState.dailyLogs,
                isLoading = uiState.isLoading,
                error = uiState.error
            )

            ListButtonsSection(
                onShowParameterDialog = { showParameterDialog = true },
                onNavigateToGraph = {
                    uiState.selectedParameter?.let { param ->
                        navController.navigate("graph_screen/${viewModel.subSystem.name}/${param.name}")
                    }
                },
                isNextDayEnabled = uiState.canNavigateForward,
                adjustDate = { days -> viewModel.selectDate(uiState.selectedDate.plusDays(days)) }
            )
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

    Column {
        Text(
            text = "Dados Atuais",
            fontSize = 22.sp,
            fontFamily = roboto,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Em ${lastLogDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
            fontSize = 14.sp,
            fontFamily = roboto,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.align(Alignment.Start)
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
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(data.entries.toList()) { entry ->
                        Card(
                            modifier = Modifier.widthIn(260.dp, Dp.Unspecified),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF83C5BE))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(entry.key, fontSize = 16.sp, color = Color.Black.copy(alpha = 0.6f))
                                Text(entry.value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListParameter(
    title: String,
    date: LocalDate,
    logs: List<Pair<String, String>>,
    isLoading: Boolean,
    error: String?
) {
    Column(modifier = Modifier.fillMaxHeight(0.6f)) {
        Text(title, fontSize = 22.sp, fontFamily = roboto, fontWeight = FontWeight.Medium)
        Text(
            text = "Data de busca: ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error) }
            logs.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum dado encontrado para este dia.") }
            else -> {
                LazyColumn {
                    items(logs) { logPair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(logPair.first, fontSize = 16.sp)
                            Text(logPair.second, fontSize = 14.sp, color = Color.Gray)
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ListButtonsSection(
    onShowParameterDialog: () -> Unit,
    onNavigateToGraph: () -> Unit,
    adjustDate: (Long) -> Unit,
    isNextDayEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        OutlinedButton(onClick = onShowParameterDialog,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
            )) {
            Text("Selecionar parâmetro", fontSize = 20.sp, fontFamily = roboto, fontWeight = FontWeight.Medium, color = Color.Black)
        }

        Button(
            onClick = onNavigateToGraph,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D77))
        ) {
            Text("Ver Gráficos", fontSize = 18.sp, color = Color.White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            OutlinedButton(onClick = { adjustDate(-1) },
                modifier = Modifier
                    .height(50.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006D77)  ,
                )) {
                Text("Dia anterior", fontSize = 20.sp, fontFamily = roboto, fontWeight = FontWeight.Medium, color = Color.White)
            }

            OutlinedButton(onClick = { adjustDate(1) },
                modifier = Modifier
                    .height(50.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006D77),
                ),
                enabled = isNextDayEnabled
            ) {
                Text("Dia posterior", fontSize = 20.sp, fontFamily = roboto, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}
