package com.campusfix.app.features.dashboard.staff.complaints

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
    viewModel: StaffComplaintsViewModel,
    complaintId: String,
    onNavigateBack: () -> Unit
) {
    val detailState by viewModel.complaintDetailState.collectAsState()
    val statusChangeState by viewModel.statusChangeState.collectAsState()

    LaunchedEffect(complaintId) {
        viewModel.loadComplaintDetail(complaintId)
    }

    // Handle status change success
    LaunchedEffect(statusChangeState) {
        if (statusChangeState is StaffComplaintActionState.Success) {
            viewModel.clearStatusChangeState()
        }
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
                is StaffComplaintDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is StaffComplaintDetailUiState.Success -> {
                    val complaint = state.complaint
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title & Status card
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
                                    StaffStatusChip(status = complaint.status)
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
                                    DetailRow("Building / Location", complaint.location)
                                }
                                DetailRow("Status", complaint.status.replace("_", " "))
                                if (!complaint.assignedStaffName.isNullOrBlank()) {
                                    DetailRow("Assigned To", complaint.assignedStaffName)
                                }
                                if (!complaint.createdAt.isNullOrBlank()) {
                                    DetailRow("Assigned Time", complaint.createdAt)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Action buttons based on status
                        when (complaint.status.uppercase()) {
                            "ASSIGNED" -> {
                                Button(
                                    onClick = {
                                        viewModel.updateStatus(complaint.id, "IN_PROGRESS")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = statusChangeState !is StaffComplaintActionState.Loading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (statusChangeState is StaffComplaintActionState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Start Work",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            "IN_PROGRESS" -> {
                                Button(
                                    onClick = {
                                        viewModel.updateStatus(complaint.id, "RESOLVED")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = statusChangeState !is StaffComplaintActionState.Loading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    if (statusChangeState is StaffComplaintActionState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onTertiary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Mark Resolved",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Error message
                        if (statusChangeState is StaffComplaintActionState.Error) {
                            Text(
                                text = (statusChangeState as StaffComplaintActionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                is StaffComplaintDetailUiState.Error -> {
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

