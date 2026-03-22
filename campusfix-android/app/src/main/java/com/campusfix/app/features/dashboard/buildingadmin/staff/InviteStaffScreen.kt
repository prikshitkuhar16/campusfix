package com.campusfix.app.features.dashboard.buildingadmin.staff

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteStaffScreen(
    viewModel: StaffViewModel,
    onNavigateBack: () -> Unit
) {
    val inviteEmail by viewModel.inviteEmail.collectAsState()
    val inviteJobType by viewModel.inviteJobType.collectAsState()
    val inviteState by viewModel.inviteState.collectAsState()

    var jobTypeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Staff") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email
            OutlinedTextField(
                value = inviteEmail,
                onValueChange = viewModel::onInviteEmailChange,
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = inviteState !is StaffActionState.Loading
            )

            // Job Type Dropdown
            ExposedDropdownMenuBox(
                expanded = jobTypeExpanded,
                onExpandedChange = { jobTypeExpanded = !jobTypeExpanded }
            ) {
                OutlinedTextField(
                    value = inviteJobType.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Job Type *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobTypeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    enabled = inviteState !is StaffActionState.Loading
                )
                ExposedDropdownMenu(
                    expanded = jobTypeExpanded,
                    onDismissRequest = { jobTypeExpanded = false }
                ) {
                    JOB_TYPES.forEach { jobType ->
                        DropdownMenuItem(
                            text = { Text(jobType.replace("_", " ")) },
                            onClick = {
                                viewModel.onInviteJobTypeChange(jobType)
                                jobTypeExpanded = false
                            }
                        )
                    }
                }
            }

            if (inviteState is StaffActionState.Error) {
                Text(
                    text = (inviteState as StaffActionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onSendInvite(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = inviteState !is StaffActionState.Loading
            ) {
                if (inviteState is StaffActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Send Invite",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


