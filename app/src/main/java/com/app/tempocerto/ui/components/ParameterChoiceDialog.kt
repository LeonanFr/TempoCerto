package com.app.tempocerto.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.tempocerto.util.roboto

@Composable
fun ParameterChoiceDialog(
    title: String,
    parameters: List<Enum<*>>,
    onDismiss: () -> Unit,
    onParameterSelected: (Enum<*>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontFamily = roboto,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(parameters) { parameter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onParameterSelected(parameter) }
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = parameter.toString(),
                            fontFamily = roboto,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}