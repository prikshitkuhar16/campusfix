package com.campusfix.app.features.dashboard.campusadmin.users

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
fun InviteUserScreen(
    viewModel: CampusAdminUsersViewModel,
    onNavigateBack: () -> Unit
) {
    val email by viewModel.inviteEmail.collectAsState()
    val selectedBuildingId by viewModel.inviteSelectedBuildingId.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val inviteState by viewModel.inviteState.collectAsState()

    var buildingDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBuildings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Building Admin") },
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
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onInviteEmailChange,
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = inviteState !is UserActionState.Loading
            )

            // Building dropdown (required)
            val selectedBuilding = buildings.find { it.id == selectedBuildingId }

            ExposedDropdownMenuBox(
                expanded = buildingDropdownExpanded,
                onExpandedChange = { buildingDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (selectedBuilding != null)
                        "${selectedBuilding.number} - ${selectedBuilding.name}"
                    else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                    enabled = inviteState !is UserActionState.Loading
                )
                ExposedDropdownMenu(
                    expanded = buildingDropdownExpanded,
                    onDismissRequest = { buildingDropdownExpanded = false }
                ) {
                    if (buildings.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No buildings available") },
                            onClick = { buildingDropdownExpanded = false }
                        )
                    } else {
                        buildings.forEach { building ->
                            DropdownMenuItem(
                                text = { Text("${building.number} - ${building.name}") },
                                onClick = {
                                    viewModel.onInviteBuildingSelected(building.id)
                                    buildingDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }


            if (inviteState is UserActionState.Error) {
                Text(
                    text = (inviteState as UserActionState.Error).message,
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
                enabled = inviteState !is UserActionState.Loading
            ) {
                if (inviteState is UserActionState.Loading) {
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

