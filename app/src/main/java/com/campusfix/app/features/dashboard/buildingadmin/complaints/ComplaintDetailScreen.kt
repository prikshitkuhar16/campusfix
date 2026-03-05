package com.campusfix.app.features.dashboard.buildingadmin.complaints

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    viewModel: ComplaintsViewModel,
    complaintId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAssignStaff: (String) -> Unit
) {
    val detailState by viewModel.complaintDetailState.collectAsState()
    val statusChangeState by viewModel.statusChangeState.collectAsState()

    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(complaintId) {
        viewModel.loadComplaintDetail(complaintId)
    }

    if (showStatusDialog) {
        val statuses = listOf("CREATED", "ASSIGNED", "IN_PROGRESS", "RESOLVED")
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Status") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { status ->
                        TextButton(
                            onClick = {
                                showStatusDialog = false
                                viewModel.updateStatus(complaintId, status)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = status.replace("_", " "),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = detailState) {
                is ComplaintDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ComplaintDetailUiState.Success -> {
                    val complaint = state.complaint
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title & Status
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = complaint.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(status = complaint.status)
                                }

                                if (!complaint.description.isNullOrBlank()) {
                                    Text(
                                        text = complaint.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Details card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!complaint.studentName.isNullOrBlank()) {
                                    DetailRow("Student", complaint.studentName)
                                }
                                if (!complaint.room.isNullOrBlank()) {
                                    DetailRow("Room", complaint.room)
                                }
                                if (!complaint.location.isNullOrBlank()) {
                                    DetailRow("Location", complaint.location)
                                }
                                DetailRow("Status", complaint.status.replace("_", " "))
                                if (!complaint.assignedStaffName.isNullOrBlank()) {
                                    DetailRow("Assigned Staff", complaint.assignedStaffName)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Action buttons
                        Button(
                            onClick = { onNavigateToAssignStaff(complaintId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Assign Staff",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = statusChangeState !is ComplaintActionState.Loading
                        ) {
                            if (statusChangeState is ComplaintActionState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Change Status",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is ComplaintDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadComplaintDetail(complaintId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

