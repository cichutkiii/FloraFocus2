package pl.preclaw.florafocus.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import java.time.LocalDate

/**
 * Komponent dialogu wyboru daty
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val day = millis / (24 * 60 * 60 * 1000)
                    val selectedDate = LocalDate.ofEpochDay(day)
                    onDateSelected(selectedDate)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Anuluj")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}