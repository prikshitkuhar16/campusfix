package com.campusfix.app.features.dashboard.student.complaints

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseComplaintScreen(
    viewModel: StudentComplaintViewModel,
    onComplaintSubmitted: () -> Unit
) {
    val room by viewModel.room.collectAsState()
    val selectedJobType by viewModel.selectedJobType.collectAsState()
    val complaint by viewModel.complaint.collectAsState()
    val availableAnytime by viewModel.availableAnytime.collectAsState()
    val availableFrom by viewModel.availableFrom.collectAsState()
    val availableTo by viewModel.availableTo.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val isBuildingSet by viewModel.isBuildingSet.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Raise Complaint",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Building Warning ──
            if (isBuildingSet == false) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Profile Incomplete",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You must set your building in your profile before creating a complaint.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Room / Location ──
            OutlinedTextField(
                value = room,
                onValueChange = viewModel::onRoomChanged,
                label = { Text("Room / Location *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Job Type dropdown ──
            JobTypeDropdown(
                selectedJobType = selectedJobType,
                onJobTypeSelected = viewModel::onJobTypeSelected,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Complaint ──
            OutlinedTextField(
                value = complaint,
                onValueChange = viewModel::onComplaintChanged,
                label = { Text("Complaint *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Available Anytime checkbox ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = availableAnytime,
                    onCheckedChange = viewModel::onAvailableAnytimeChanged,
                    enabled = submitState !is SubmitComplaintUiState.Loading
                )
                Text(
                    text = "Available Anytime",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ── Available From / To (HH:mm) ──
            if (!availableAnytime) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimePickerField(
                        label = "From *",
                        value = availableFrom,
                        onTimeSelected = viewModel::onAvailableFromChanged,
                        enabled = submitState !is SubmitComplaintUiState.Loading,
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        label = "To *",
                        value = availableTo,
                        onTimeSelected = viewModel::onAvailableToChanged,
                        enabled = submitState !is SubmitComplaintUiState.Loading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Error message ──
            if (submitState is SubmitComplaintUiState.Error) {
                Text(
                    text = (submitState as SubmitComplaintUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Submit button ──
            Button(
                onClick = { viewModel.submitComplaint(onSuccess = onComplaintSubmitted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = submitState !is SubmitComplaintUiState.Loading
            ) {
                if (submitState is SubmitComplaintUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Submit",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobTypeDropdown(
    selectedJobType: String?,
    onJobTypeSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedJobType?.replace("_", " ") ?: "",
            onValueChange = {},
            label = { Text("Job Type *") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            jobTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.replace("_", " ")) },
                    onClick = {
                        onJobTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = value.substringBefore(":").toIntOrNull() ?: 9,
        initialMinute = value.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = false
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        placeholder = { Text("HH:mm") },
        modifier = modifier,
        readOnly = true,
        enabled = enabled,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Select time"
                )
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$hour:$minute")
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
