package com.campusfix.app.features.dashboard.buildingadmin.complaints

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.core.net.toUri
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
    viewModel: ComplaintsViewModel,
    complaintId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAssignStaff: (complaintId: String, jobType: String?, isReassign: Boolean) -> Unit
) {
    val detailState by viewModel.complaintDetailState.collectAsState()
    
    LaunchedEffect(complaintId) {
        viewModel.loadComplaintDetail(complaintId)
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
                                        text = complaint.complaint,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(status = complaint.status)
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
                                complaint.student?.let { student ->
                                    if (!student.name.isNullOrBlank()) {
                                        DetailRow("Student", student.name)
                                    }
                                    if (!student.phoneNumber.isNullOrBlank()) {
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
                                                    text = student.phoneNumber,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                            IconButton(onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = "tel:${student.phoneNumber}".toUri()
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
                                }
                                if (!complaint.room.isNullOrBlank()) {
                                    DetailRow("Room", complaint.room)
                                }
                                if (!complaint.location.isNullOrBlank()) {
                                    DetailRow("Location", complaint.location)
                                }
                                if (!complaint.jobType.isNullOrBlank()) {
                                    DetailRow("Job Type", complaint.jobType.replace("_", " "))
                                }
                                DetailRow("Status", complaint.status.replace("_", " "))
                                complaint.assignedStaff?.let { staff ->
                                    if (!staff.name.isNullOrBlank()) {
                                        DetailRow("Assigned Staff", staff.name)
                                    }
                                    if (!staff.phoneNumber.isNullOrBlank()) {
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
                                                    text = staff.phoneNumber,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                            IconButton(onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = "tel:${staff.phoneNumber}".toUri()
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
                                }
                                // Availability
                                if (complaint.availableAnytime == true) {
                                    DetailRow("Student Availability", "Anytime")
                                } else if (!complaint.availableFrom.isNullOrBlank() && !complaint.availableTo.isNullOrBlank()) {
                                    DetailRow("Student Availability", "${complaint.availableFrom} – ${complaint.availableTo}")
                                }
                                if (!complaint.createdAt.isNullOrBlank()) {
                                    DetailRow("Created", complaint.createdAt)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Assign / Reassign button logic
                        val status = complaint.status.uppercase()
                        
                        if (status != "VERIFIED") {
                            val isReassign = status != "CREATED"
                            val buttonText = when (status) {
                                "CREATED" -> "Assign Staff"
                                "ASSIGNED" -> "Reassign Staff"
                                "RESOLVED" -> "Reopen & Assign Staff"
                                else -> "Assign Staff"
                            }

                            Button(
                                onClick = { onNavigateToAssignStaff(complaintId, complaint.jobType, isReassign) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = buttonText,
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
