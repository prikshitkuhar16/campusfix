package com.campusfix.app.features.dashboard.student.complaints

import android.content.Intent
import android.net.Uri
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
    viewModel: StudentComplaintViewModel,
    complaintId: String,
    onNavigateBack: () -> Unit
) {
    val detailState by viewModel.complaintDetailState.collectAsState()
    val verifyState by viewModel.verifyState.collectAsState()
    val reopenState by viewModel.reopenState.collectAsState()

    LaunchedEffect(complaintId) {
        viewModel.loadComplaintDetail(complaintId)
    }

    LaunchedEffect(verifyState) {
        if (verifyState is VerifyResolutionUiState.Success) {
            viewModel.clearVerifyState()
        }
    }

    LaunchedEffect(reopenState) {
        if (reopenState is ReopenComplaintUiState.Success) {
            viewModel.clearReopenState()
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
                is StudentComplaintDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is StudentComplaintDetailUiState.Success -> {
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
                                        text = complaint.complaint,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StudentStatusChip(status = complaint.status)
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
                                if (!complaint.location.isNullOrBlank()) {
                                    DetailRow("Building", complaint.location)
                                }
                                if (!complaint.room.isNullOrBlank()) {
                                    DetailRow("Room", complaint.room)
                                }
                                if (!complaint.jobType.isNullOrBlank()) {
                                    DetailRow("Job Type", complaint.jobType.replace("_", " "))
                                }
                                DetailRow("Status", complaint.status.replace("_", " "))
                                if (!complaint.assignedStaff?.name.isNullOrBlank()) {
                                    DetailRow("Assigned Staff", complaint.assignedStaff?.name ?: "")
                                }
                                if (!complaint.assignedStaff?.phoneNumber.isNullOrBlank()) {
                                    val context = LocalContext.current
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Staff Phone",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = complaint.assignedStaff?.phoneNumber ?: "",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${complaint.assignedStaff?.phoneNumber}")
                                            }
                                            context.startActivity(intent)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Call Staff",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                // Availability
                                if (complaint.availableAnytime == true) {
                                    DetailRow("Availability", "Anytime")
                                } else if (!complaint.availableFrom.isNullOrBlank() && !complaint.availableTo.isNullOrBlank()) {
                                    DetailRow("Availability", "${complaint.availableFrom} – ${complaint.availableTo}")
                                }
                                if (!complaint.createdAt.isNullOrBlank()) {
                                    DetailRow("Created", complaint.createdAt)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Verify Resolution button (only when status is RESOLVED)
                        if (complaint.status.uppercase() == "RESOLVED") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.verifyResolution(complaint.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = verifyState !is VerifyResolutionUiState.Loading && reopenState !is ReopenComplaintUiState.Loading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (verifyState is VerifyResolutionUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Verify Resolution",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.reopenComplaint(complaint.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = verifyState !is VerifyResolutionUiState.Loading && reopenState !is ReopenComplaintUiState.Loading,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                ) {
                                    if (reopenState is ReopenComplaintUiState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.error,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Not Satisfied (Reopen)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Error message
                        if (verifyState is VerifyResolutionUiState.Error) {
                            Text(
                                text = (verifyState as VerifyResolutionUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (reopenState is ReopenComplaintUiState.Error) {
                           Text(
                               text = (reopenState as ReopenComplaintUiState.Error).message,
                               color = MaterialTheme.colorScheme.error,
                               style = MaterialTheme.typography.bodySmall
                           )
                       }
                    }
                }

                is StudentComplaintDetailUiState.Error -> {
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
