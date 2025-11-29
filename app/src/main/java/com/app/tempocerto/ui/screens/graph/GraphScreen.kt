package com.app.tempocerto.ui.screens.graph

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.app.tempocerto.ui.components.AppBottomBar
import com.app.tempocerto.ui.components.BlobBackground
import com.app.tempocerto.ui.components.ParameterChoiceDialog
import com.app.tempocerto.ui.components.SearchModalBottomSheet
import com.app.tempocerto.ui.theme.Teal
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

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

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
                    when {
                        uiState.isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        (uiState.error != null && uiState.error!!.contains("Restrito")) || uiState.isRestricted -> {
                            RestrictedGraphAccessView(
                                onRequestAccess = { days -> viewModel.requestAccessToData(days) }
                            )
                        }
                        uiState.error != null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = uiState.error ?: "Erro desconhecido", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {
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
        }

        if (showSearchModal) {
            SearchModalBottomSheet(
                onDismiss = { showSearchModal = false },
                onApplySearch = { start, end ->
                    val dateToUse = start ?: LocalDate.now()
                    if (start != null && end != null) {
                        viewModel.fetchLogsByDateRange(start, end)
                    } else {
                        viewModel.selectDate(dateToUse)
                    }
                    showSearchModal = false
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
private fun RestrictedGraphAccessView(
    onRequestAccess: (Int) -> Unit
) {
    var daysText by remember { mutableStateOf("30") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Bloqueado",
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Acesso Restrito",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Teal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dados com mais de 7 dias requerem permissão especial.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = daysText,
                onValueChange = { if (it.all { char -> char.isDigit() }) daysText = it },
                label = { Text("Dias desejados") },
                singleLine = true,
                modifier = Modifier.width(200.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val days = daysText.toIntOrNull() ?: 30
                    onRequestAccess(days)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text("Solicitar Acesso")
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

    val isMultiDay = remember(entries) {
        if (entries.isEmpty()) false else (entries.last().x - entries.first().x) > 86400
    }

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
                            return try {
                                sdf.format(Date(value.toLong() * 1000))
                            } catch (_: Exception) { "" }
                        }
                    }
                }
                axisLeft.textColor = textArgb
            }
        },
        update = { chart ->
            if (entries.isEmpty()) {
                chart.clear()
            } else {
                val dataSet = LineDataSet(entries, "Dados").apply {
                    color = lineArgb
                    setDrawValues(false)
                    setDrawCircles(false)
                    lineWidth = 2f
                }
                chart.data = LineData(dataSet)

                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return try {
                            val date = Date(value.toLong() * 1000)
                            if (isMultiDay) dateFormat.format(date) else timeFormat.format(date)
                        } catch (_: Exception) { "" }
                    }
                }

                chart.invalidate()
            }
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
