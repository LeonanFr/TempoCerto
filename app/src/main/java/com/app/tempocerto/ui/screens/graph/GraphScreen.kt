package com.app.tempocerto.ui.screens.graph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.app.tempocerto.ui.components.BottomBar
import com.app.tempocerto.ui.components.ParameterChoiceDialog
import com.app.tempocerto.ui.components.SearchModalBottomSheet
import com.app.tempocerto.util.roboto
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gráficos") })
        },
        bottomBar = {
            BottomAppBar(contentPadding = PaddingValues(0.dp)) {
                BottomBar(
                    navController = navController,
                    onSearchClicked = { showSearchModal = true }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            Text(
                text = "Em ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LineChartComponent(entries = uiState.chartEntries)

                Spacer(modifier = Modifier.height(24.dp))

                GraphActualParameter(
                    parameterName = uiState.selectedParameter?.toString() ?: "Parâmetro",
                    value = uiState.lastLogValue,
                    time = uiState.lastLogTime
                )

                Spacer(modifier = Modifier.height(24.dp))

                GraphButtonsSection(
                    onShowParameterDialog = { showParameterDialog = true },
                    adjustDate = { days -> viewModel.selectDate(uiState.selectedDate.plusDays(days)) },
                    isNextDayEnabled = uiState.canNavigateForward,
                    onNavigateToList = {
                        uiState.selectedParameter?.let { param ->
                            navController.navigate("list_screen/${viewModel.subSystem.name}/${param.name}")
                        }
                    }
                )
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
private fun LineChartComponent(entries: List<Entry>) {
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
                color = android.graphics.Color.BLUE
                setDrawValues(false)
                setDrawCircles(false)
                lineWidth = 2f
            }
            chart.data = LineData(dataSet)
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
    Column {
        Text(parameterName, fontSize = 20.sp, color = Color.Gray)
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(time, fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
private fun GraphButtonsSection(
    onShowParameterDialog: () -> Unit,
    adjustDate: (Long) -> Unit,
    isNextDayEnabled : Boolean,
    onNavigateToList: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
    ) {

        Button(onClick = onNavigateToList,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006D77)
            )) {
            Text("Ver em Lista", fontSize = 20.sp, fontFamily = roboto, fontWeight = FontWeight.Medium, color = Color.White)
        }

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
                enabled = isNextDayEnabled,
                modifier = Modifier
                    .height(50.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006D77),
                )) {
                Text("Dia posterior", fontSize = 20.sp, fontFamily = roboto, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}
