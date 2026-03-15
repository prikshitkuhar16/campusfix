package com.campusfix.app.features.dashboard.buildingadmin.staff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusfix.app.data.remote.dto.InviteDto
import com.campusfix.app.data.remote.dto.StaffDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    viewModel: StaffViewModel,
    onNavigateToInviteStaff: () -> Unit,
    onNavigateToStaffDetail: (String) -> Unit
) {
    val staffState by viewModel.staffState.collectAsState()
    val updateJobTypeState by viewModel.updateJobTypeState.collectAsState()
    val invitesState by viewModel.invitesState.collectAsState()
    val revokeState by viewModel.revokeState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Inactive", "Invited")

    var showEditJobDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<StaffDto?>(null) }
    var selectedJobType by remember { mutableStateOf("") }

    var showRevokeDialog by remember { mutableStateOf(false) }
    var revokeInviteId by remember { mutableStateOf<String?>(null) }
    var revokeInviteEmail by remember { mutableStateOf("") }

    if (showEditJobDialog && staffToEdit != null) {
        val staff = staffToEdit!!
        AlertDialog(
            onDismissRequest = { 
                showEditJobDialog = false 
                selectedJobType = ""
            },
            title = { Text("Edit Job Type") },
            text = {
                Column {
                    Text("Select new job type for ${staff.name ?: staff.email}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple dropdown or radio list. Since JOB_TYPES is a list, let's use a simple list
                    // For better UI, maybe a dropdown menu or modal bottom sheet, but user asked for button action.
                    // Dialog with radio buttons is clear.
                    
                   LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                       items(JOB_TYPES) { jobType ->
                           Row(
                               Modifier
                                   .fillMaxWidth()
                                   .clickable { selectedJobType = jobType }
                                   .padding(vertical = 4.dp),
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               RadioButton(
                                   selected = (jobType == selectedJobType),
                                   onClick = { selectedJobType = jobType }
                               )
                               Text(
                                   text = jobType,
                                   style = MaterialTheme.typography.bodyMedium,
                                   modifier = Modifier.padding(start = 8.dp)
                               )
                           }
                       }
                   }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                         if (selectedJobType.isNotBlank()) {
                             viewModel.updateStaffJobType(staff.id, selectedJobType) {
                                 showEditJobDialog = false
                                 selectedJobType = ""
                                 staffToEdit = null
                             }
                         }
                    },
                    enabled = selectedJobType.isNotBlank() && updateJobTypeState !is StaffActionState.Loading
                ) {
                    if (updateJobTypeState is StaffActionState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEditJobDialog = false 
                        selectedJobType = ""
                        staffToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRevokeDialog && revokeInviteId != null) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text("Revoke Invitation") },
            text = { Text("Are you sure you want to revoke the invitation sent to $revokeInviteEmail?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRevokeDialog = false
                        viewModel.revokeInvite(revokeInviteId!!)
                        revokeInviteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = revokeState !is StaffActionState.Loading
                ) {
                    Text("Revoke")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRevokeDialog = false
                    revokeInviteId = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Staff",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToInviteStaff,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Invite Staff"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            if (index == 2) {
                                viewModel.loadInvites()
                            }
                        },
                        label = { Text(title) }
                    )
                }
            }

            if (selectedTabIndex == 2) {
                // Invites UI
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = invitesState) {
                        is StaffInvitesUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is StaffInvitesUiState.Empty -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No pending invitations",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is StaffInvitesUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(state.invites, key = { it.id }) { invite ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = invite.email,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = invite.role.replace("_", " "),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (!invite.jobType.isNullOrBlank()) {
                                                    Text(
                                                        text = invite.jobType,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                            }
                                            AssistChip(
                                                onClick = {
                                                    revokeInviteId = invite.id
                                                    revokeInviteEmail = invite.email
                                                    showRevokeDialog = true
                                                },
                                                label = { Text("Revoke") },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is StaffInvitesUiState.Error -> {
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
                                Button(onClick = { viewModel.loadInvites() }) {
                                    Text("Retry")
                                }
                            }
                        }
                        else -> {}
                    }
                }
            } else {
                // Staff UI (Active/Inactive)
                when (val state = staffState) {
                    is StaffUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    is StaffUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No staff found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to invite staff",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is StaffUiState.Success -> {
                        val filteredStaff = state.staff.filter {
                            if (selectedTabIndex == 0) it.isActive else !it.isActive
                        }

                        if (filteredStaff.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No ${tabs[selectedTabIndex].lowercase()} staff found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(filteredStaff, key = { it.id }) { staff ->
                                    StaffCard(
                                        staff = staff,
                                        onClick = {
                                            viewModel.setSelectedStaff(staff)
                                            onNavigateToStaffDetail(staff.id)
                                        },
                                        onEditJobType = {
                                            staffToEdit = staff
                                            selectedJobType = staff.jobType ?: "PLUMBER"
                                            showEditJobDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is StaffUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                Button(onClick = { viewModel.loadStaff() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffCard(
    staff: StaffDto,
    onClick: () -> Unit,
    onEditJobType: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = staff.name ?: "Unnamed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = staff.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (!staff.jobType.isNullOrBlank()) {
                    AssistChip(
                        onClick = onEditJobType,
                        label = {
                            Text(
                                text = staff.jobType,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                } else {
                    // Show Add Job Type button if none exists? 
                    // Or just a placeholder. User said "edit jobtype", implying it exists or can be set.
                    // If null, we can show "Assign Job" or "No Job".
                     AssistChip(
                        onClick = onEditJobType,
                        label = { Text("Assign Job") },
                        trailingIcon = {
                             Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                     )
                }

            }
        }
    }
}
