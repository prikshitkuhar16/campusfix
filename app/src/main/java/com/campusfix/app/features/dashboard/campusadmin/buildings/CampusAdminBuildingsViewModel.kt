package com.campusfix.app.features.dashboard.campusadmin.buildings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.BuildingDetailResponse
import com.campusfix.app.data.remote.dto.BuildingDto
import com.campusfix.app.data.remote.dto.CampusUserDto
import com.campusfix.app.domain.repository.CampusAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CampusAdminBuildingsViewModel(
    private val repository: CampusAdminRepository
) : ViewModel() {

    // ── Buildings list ──
    private val _buildingsState = MutableStateFlow<BuildingsUiState>(BuildingsUiState.Loading)
    val buildingsState: StateFlow<BuildingsUiState> = _buildingsState.asStateFlow()

    // ── Building detail ──
    private val _buildingDetailState = MutableStateFlow<BuildingDetailUiState>(BuildingDetailUiState.Loading)
    val buildingDetailState: StateFlow<BuildingDetailUiState> = _buildingDetailState.asStateFlow()

    // ── Add building form ──
    private val _addBuildingName = MutableStateFlow("")
    val addBuildingName: StateFlow<String> = _addBuildingName.asStateFlow()

    private val _addBuildingDescription = MutableStateFlow("")
    val addBuildingDescription: StateFlow<String> = _addBuildingDescription.asStateFlow()

    private val _addBuildingNumber = MutableStateFlow("")
    val addBuildingNumber: StateFlow<String> = _addBuildingNumber.asStateFlow()

    private val _addBuildingState = MutableStateFlow<FormActionState>(FormActionState.Idle)
    val addBuildingState: StateFlow<FormActionState> = _addBuildingState.asStateFlow()

    // ── Delete state ──
    private val _deleteState = MutableStateFlow<FormActionState>(FormActionState.Idle)
    val deleteState: StateFlow<FormActionState> = _deleteState.asStateFlow()

    // ── Assign admin ──
    private val _availableAdmins = MutableStateFlow<List<CampusUserDto>>(emptyList())
    val availableAdmins: StateFlow<List<CampusUserDto>> = _availableAdmins.asStateFlow()

    private val _availableAdminsLoading = MutableStateFlow(false)
    val availableAdminsLoading: StateFlow<Boolean> = _availableAdminsLoading.asStateFlow()

    private val _assignAdminState = MutableStateFlow<FormActionState>(FormActionState.Idle)
    val assignAdminState: StateFlow<FormActionState> = _assignAdminState.asStateFlow()

    // ── Snackbar events ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadBuildings()
    }

    fun loadBuildings() {
        viewModelScope.launch {
            _buildingsState.value = BuildingsUiState.Loading
            when (val result = repository.getBuildings()) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _buildingsState.value = BuildingsUiState.Empty
                    } else {
                        _buildingsState.value = BuildingsUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _buildingsState.value = BuildingsUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadBuildingDetail(buildingId: String) {
        viewModelScope.launch {
            _buildingDetailState.value = BuildingDetailUiState.Loading
            when (val result = repository.getBuildingById(buildingId)) {
                is Resource.Success -> {
                    _buildingDetailState.value = BuildingDetailUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _buildingDetailState.value = BuildingDetailUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Add building form handlers ──

    fun onAddBuildingNameChange(value: String) {
        _addBuildingName.value = value.replace("\n", "").replace("\r", "")
    }

    fun onAddBuildingDescriptionChange(value: String) {
        _addBuildingDescription.value = value.replace("\n", "").replace("\r", "")
    }

    fun onAddBuildingNumberChange(value: String) {
        _addBuildingNumber.value = value.replace("\n", "").replace("\r", "")
    }

    fun onSaveBuilding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val name = _addBuildingName.value.trim()
            if (name.isBlank()) {
                _addBuildingState.value = FormActionState.Error("Name is required")
                return@launch
            }

            _addBuildingState.value = FormActionState.Loading
            when (val result = repository.createBuilding(
                name = name,
                description = _addBuildingDescription.value.trim(),
                number = _addBuildingNumber.value.trim()
            )) {
                is Resource.Success -> {
                    _addBuildingState.value = FormActionState.Success(result.data)
                    _addBuildingName.value = ""
                    _addBuildingDescription.value = ""
                    _addBuildingNumber.value = ""
                    _snackbarEvent.emit("Building created successfully")
                    loadBuildings()
                    onSuccess()
                }
                is Resource.Error -> {
                    _addBuildingState.value = FormActionState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteBuilding(buildingId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteState.value = FormActionState.Loading
            when (val result = repository.deleteBuilding(buildingId)) {
                is Resource.Success -> {
                    _deleteState.value = FormActionState.Success(result.data)
                    _snackbarEvent.emit("Building deleted successfully")
                    loadBuildings()
                    onSuccess()
                }
                is Resource.Error -> {
                    _deleteState.value = FormActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearAddBuildingState() {
        _addBuildingState.value = FormActionState.Idle
    }

    fun clearDeleteState() {
        _deleteState.value = FormActionState.Idle
    }

    // ── Assign Building Admin ──

    fun loadAvailableAdmins() {
        viewModelScope.launch {
            _availableAdminsLoading.value = true
            when (val result = repository.getUsers("BUILDING_ADMIN")) {
                is Resource.Success -> {
                    _availableAdmins.value = result.data.filter { it.isActive }
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
            _availableAdminsLoading.value = false
        }
    }

    fun assignBuildingAdmin(buildingId: String, userId: String) {
        viewModelScope.launch {
            _assignAdminState.value = FormActionState.Loading
            when (val result = repository.assignBuildingAdmin(buildingId, userId)) {
                is Resource.Success -> {
                    _assignAdminState.value = FormActionState.Success("Admin assigned successfully")
                    _snackbarEvent.emit("Building admin assigned successfully")
                    // Immediately update UI with response
                    _buildingDetailState.value = BuildingDetailUiState.Success(result.data)
                    // Reload to ensure consistency across screens
                    loadBuildingDetail(buildingId)
                    loadBuildings()
                }
                is Resource.Error -> {
                    _assignAdminState.value = FormActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearAssignAdminState() {
        _assignAdminState.value = FormActionState.Idle
    }
}

// ── UI States ──

sealed class BuildingsUiState {
    data object Loading : BuildingsUiState()
    data object Empty : BuildingsUiState()
    data class Success(val buildings: List<BuildingDto>) : BuildingsUiState()
    data class Error(val message: String) : BuildingsUiState()
}

sealed class BuildingDetailUiState {
    data object Loading : BuildingDetailUiState()
    data class Success(val building: BuildingDetailResponse) : BuildingDetailUiState()
    data class Error(val message: String) : BuildingDetailUiState()
}

sealed class FormActionState {
    data object Idle : FormActionState()
    data object Loading : FormActionState()
    data class Success(val message: String) : FormActionState()
    data class Error(val message: String) : FormActionState()
}
