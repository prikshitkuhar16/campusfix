package com.campusfix.app.features.dashboard.student.complaints

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
    viewModel: StudentComplaintViewModel,
    complaintId: String,
    onNavigateBack: () -> Unit
) {
    val detailState by viewModel.complaintDetailState.collectAsState()
    val verifyState by viewModel.verifyState.collectAsState()

    LaunchedEffect(complaintId) {
        viewModel.loadComplaintDetail(complaintId)
    }

    LaunchedEffect(verifyState) {
        if (verifyState is VerifyResolutionUiState.Success) {
            viewModel.clearVerifyState()
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
                                        text = complaint.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StudentStatusChip(status = complaint.status)
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
                                if (!complaint.location.isNullOrBlank()) {
                                    DetailRow("Building", complaint.location)
                                }
                                if (!complaint.room.isNullOrBlank()) {
                                    DetailRow("Room", complaint.room)
                                }
                                DetailRow("Status", complaint.status.replace("_", " "))
                                if (!complaint.assignedStaffName.isNullOrBlank()) {
                                    DetailRow("Assigned Staff", complaint.assignedStaffName)
                                }
                                if (!complaint.createdAt.isNullOrBlank()) {
                                    DetailRow("Created", complaint.createdAt)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Verify Resolution button (only when status is RESOLVED)
                        if (complaint.status.uppercase() == "RESOLVED") {
                            Button(
                                onClick = { viewModel.verifyResolution(complaint.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = verifyState !is VerifyResolutionUiState.Loading,
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
                        }

                        // Error message
                        if (verifyState is VerifyResolutionUiState.Error) {
                            Text(
                                text = (verifyState as VerifyResolutionUiState.Error).message,
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

