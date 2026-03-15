package com.campusfix.app.features.dashboard.staff.complaints

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                                        text = complaint.complaint,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StaffStatusChip(status = complaint.status)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!complaint.student?.name.isNullOrBlank()) {
                                    DetailRow("Student", complaint.student?.name ?: "")
                                }
                                if (!complaint.student?.phoneNumber.isNullOrBlank()) {
                                    val context = LocalContext.current
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Student Phone",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = complaint.student?.phoneNumber ?: "",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = "tel:${complaint.student?.phoneNumber}".toUri()
                                            }
                                            context.startActivity(intent)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Call Student",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                if (!complaint.room.isNullOrBlank()) {
                                    DetailRow("Room", complaint.room)
                                }
                                if (!complaint.location.isNullOrBlank()) {
                                    DetailRow("Building / Location", complaint.location)
                                }
                                if (!complaint.jobType.isNullOrBlank()) {
                                    DetailRow("Job Type", complaint.jobType.replace("_", " "))
                                }
                                DetailRow("Status", complaint.status.replace("_", " "))
                                if (!complaint.assignedStaff?.name.isNullOrBlank()) {
                                    DetailRow("Assigned To", complaint.assignedStaff?.name ?: "")
                                }
                                if (complaint.availableAnytime == true) {
                                    DetailRow("Student Availability", "Anytime")
                                } else if (!complaint.availableFrom.isNullOrBlank() && !complaint.availableTo.isNullOrBlank()) {
                                    DetailRow("Student Availability", "${complaint.availableFrom} – ${complaint.availableTo}")
                                }
                                if (!complaint.createdAt.isNullOrBlank()) {
                                    DetailRow("Assigned Time", complaint.createdAt)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        when (complaint.status.uppercase()) {
                            "ASSIGNED" -> {
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
