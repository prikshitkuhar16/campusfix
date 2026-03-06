package com.campusfix.app.features.dashboard.student.complaints

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusfix.app.data.remote.dto.BuildingDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseComplaintScreen(
    viewModel: StudentComplaintViewModel,
    onComplaintSubmitted: () -> Unit
) {
    val buildingsState by viewModel.buildingsState.collectAsState()
    val selectedBuilding by viewModel.selectedBuilding.collectAsState()
    val room by viewModel.room.collectAsState()
    val selectedJobType by viewModel.selectedJobType.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBuildings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Raise Complaint",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Building dropdown ──
            BuildingDropdown(
                buildingsState = buildingsState,
                selectedBuilding = selectedBuilding,
                onBuildingSelected = viewModel::onBuildingSelected
            )

            // ── Room / Location ──
            OutlinedTextField(
                value = room,
                onValueChange = viewModel::onRoomChanged,
                label = { Text("Room / Location *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Job Type dropdown ──
            JobTypeDropdown(
                selectedJobType = selectedJobType,
                onJobTypeSelected = viewModel::onJobTypeSelected,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Title ──
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Description ──
            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = submitState !is SubmitComplaintUiState.Loading
            )

            // ── Error message ──
            if (submitState is SubmitComplaintUiState.Error) {
                Text(
                    text = (submitState as SubmitComplaintUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Submit button ──
            Button(
                onClick = { viewModel.submitComplaint(onSuccess = onComplaintSubmitted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = submitState !is SubmitComplaintUiState.Loading
            ) {
                if (submitState is SubmitComplaintUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Submit",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuildingDropdown(
    buildingsState: BuildingsUiState,
    selectedBuilding: BuildingDto?,
    onBuildingSelected: (BuildingDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    when (buildingsState) {
        is BuildingsUiState.Loading -> {
            OutlinedTextField(
                value = "Loading buildings...",
                onValueChange = {},
                label = { Text("Building *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true
            )
        }

        is BuildingsUiState.Error -> {
            OutlinedTextField(
                value = "Failed to load buildings",
                onValueChange = {},
                label = { Text("Building *") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true,
                isError = true
            )
        }

        is BuildingsUiState.Success -> {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedBuilding?.let { "${it.number} - ${it.name}" } ?: "",
                    onValueChange = {},
                    label = { Text("Building *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    buildingsState.buildings.forEach { building ->
                        DropdownMenuItem(
                            text = { Text("${building.number} - ${building.name}") },
                            onClick = {
                                onBuildingSelected(building)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobTypeDropdown(
    selectedJobType: String?,
    onJobTypeSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedJobType?.replace("_", " ") ?: "",
            onValueChange = {},
            label = { Text("Job Type *") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            jobTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.replace("_", " ")) },
                    onClick = {
                        onJobTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}



