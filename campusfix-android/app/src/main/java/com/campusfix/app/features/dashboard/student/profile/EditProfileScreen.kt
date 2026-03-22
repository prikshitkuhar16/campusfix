package com.campusfix.app.features.dashboard.student.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: StudentProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val editName by viewModel.editName.collectAsState()
    val editPhoneNumber by viewModel.editPhoneNumber.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val selectedBuildingId by viewModel.selectedBuildingId.collectAsState()

    var buildingDropdownExpanded by remember { mutableStateOf(false) }
    val selectedBuildingName = buildings.find { it.id == selectedBuildingId }?.name ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
                value = editName,
                onValueChange = viewModel::onEditNameChange,
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = editState !is StudentEditProfileUiState.Loading
            )

            OutlinedTextField(
                value = editPhoneNumber,
                onValueChange = viewModel::onEditPhoneNumberChange,
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = editState !is StudentEditProfileUiState.Loading
            )

            // Building dropdown
            if (buildings.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = buildingDropdownExpanded,
                    onExpandedChange = {
                        if (editState !is StudentEditProfileUiState.Loading) {
                            buildingDropdownExpanded = !buildingDropdownExpanded
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = selectedBuildingName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Building") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = editState !is StudentEditProfileUiState.Loading
                    )
                    ExposedDropdownMenu(
                        expanded = buildingDropdownExpanded,
                        onDismissRequest = { buildingDropdownExpanded = false }
                    ) {
                        buildings.forEach { building ->
                            DropdownMenuItem(
                                text = { Text(building.name) },
                                onClick = {
                                    viewModel.onBuildingSelected(building.id)
                                    buildingDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (editState is StudentEditProfileUiState.Error) {
                Text(
                    text = (editState as StudentEditProfileUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onSaveProfile(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = editState !is StudentEditProfileUiState.Loading
            ) {
                if (editState is StudentEditProfileUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

