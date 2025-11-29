package com.app.tempocerto.ui.components

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.tempocerto.R
import com.app.tempocerto.ui.theme.Teal
import com.app.tempocerto.util.roboto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

enum class SearchType {
    SPECIFIC_DAY,
    PERIOD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchModalBottomSheet(
    onDismiss: () -> Unit,
    onApplySearch: (LocalDate?, LocalDate?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    var searchType by remember { mutableStateOf(SearchType.SPECIFIC_DAY) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    fun showDatePicker(currentDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
        val calendar = Calendar.getInstance()
        currentDate?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Filtrar Dados",
                fontFamily = roboto,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Teal
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    text = "Dia Específico",
                    isSelected = searchType == SearchType.SPECIFIC_DAY,
                    onClick = { searchType = SearchType.SPECIFIC_DAY }
                )
                TabButton(
                    text = "Período",
                    isSelected = searchType == SearchType.PERIOD,
                    onClick = { searchType = SearchType.PERIOD }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (searchType == SearchType.SPECIFIC_DAY) {
                DateSelectorField(
                    label = "Selecione a Data",
                    date = selectedDate,
                    onSpaceClick = {
                        showDatePicker(selectedDate) { selectedDate = it }
                    }
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        DateSelectorField(
                            label = "Início",
                            date = startDate,
                            onSpaceClick = {
                                showDatePicker(startDate) { startDate = it }
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DateSelectorField(
                            label = "Fim",
                            date = endDate,
                            onSpaceClick = {
                                showDatePicker(endDate) { endDate = it }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (searchType == SearchType.SPECIFIC_DAY) {
                        onApplySearch(selectedDate, null)
                    } else {
                        onApplySearch(startDate, endDate)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(
                    text = "Buscar",
                    fontFamily = roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RowScope.TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .background(
                if (isSelected) Color.White else Color.Transparent,
                RoundedCornerShape(50)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Teal else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = roboto
        )
    }
}

@Composable
fun DateSelectorField(label: String, date: LocalDate?, onSpaceClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onSpaceClick() }) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = roboto
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F6F8), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "--/--/----",
                color = if (date != null) Teal else Color.Gray,
                fontFamily = roboto,
                fontWeight = FontWeight.Medium
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null,
                tint = Teal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}