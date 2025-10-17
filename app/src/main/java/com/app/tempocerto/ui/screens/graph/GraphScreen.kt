package com.app.tempocerto.ui.screens.graph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.app.tempocerto.ui.components.AppBottomBar
import com.app.tempocerto.ui.components.BlobBackground
import com.app.tempocerto.ui.components.ParameterChoiceDialog
import com.app.tempocerto.ui.components.SearchModalBottomSheet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    navController: NavHostController,
    viewModel: GraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showParameterDialog by remember { mutableStateOf(false) }
    var showSearchModal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        BlobBackground()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(uiState.selectedParameter?.toString() ?: "Gráficos") },
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
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.padding(horizontal = 15.dp)) {
                    DateNavigationHeader(
                        date = uiState.selectedDate,
                        isNextDayEnabled = uiState.canNavigateForward,
                        adjustDate = { days -> viewModel.selectDate(uiState.selectedDate.plusDays(days)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 15.dp, vertical = 24.dp)
                        ) {
                            LineChartComponent(
                                entries = uiState.chartEntries,
                                lineColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            GraphActualParameter(
                                parameterName = uiState.selectedParameter?.toString() ?: "Parâmetro",
                                value = uiState.lastLogValue,
                                time = uiState.lastLogTime
                            )
                        }
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

@Composable
private fun DateNavigationHeader(
    date: LocalDate,
    isNextDayEnabled: Boolean,
    adjustDate: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { adjustDate(-1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Dia anterior")
        }

        Text(
            text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        IconButton(onClick = { adjustDate(1) }, enabled = isNextDayEnabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Dia posterior",
                tint = if (isNextDayEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun LineChartComponent(entries: List<Entry>, lineColor: Color, textColor: Color) {
    val lineArgb = lineColor.toArgb()
    val textArgb = textColor.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    valueFormatter = object : ValueFormatter() {
                        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return sdf.format(Date(value.toLong() * 1000))
                        }
                    }
                }
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Dados").apply {
                color = lineArgb
                setDrawValues(false)
                setDrawCircles(false)
                lineWidth = 2f
            }
            chart.data = LineData(dataSet)
            chart.axisLeft.textColor = textArgb
            chart.xAxis.textColor = textArgb
            chart.invalidate()
        }
    )
}

@Composable
private fun GraphActualParameter(
    parameterName: String,
    value: String,
    time: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(parameterName, fontSize = 20.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, fontSize = 16.sp, color = Color.Gray)
        }
    }
}
