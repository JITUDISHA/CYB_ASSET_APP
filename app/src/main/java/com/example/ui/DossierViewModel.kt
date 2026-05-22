package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DossierViewModel(application: Application) : AndroidViewModel(application) {

  private val database = DossierDatabase.getDatabase(application)
  private val repository = DossierRepository(database.dossierDao())

  // Authentication State
  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _currentUsername = MutableStateFlow<String?>(null)
  val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

  private val _currentClearance = MutableStateFlow<String?>(null)
  val currentClearance: StateFlow<String?> = _currentClearance.asStateFlow()

  // App Theme Switchable State (Defaults to Cinematic Noir Dark Mode)
  private val _isDarkMode = MutableStateFlow(true)
  val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

  // UI Active Tab: "ARCHIVE" | "ASSETS" | "INTEL" | "AGENT"
  private val _currentTab = MutableStateFlow("ASSETS")
  val currentTab: StateFlow<String> = _currentTab.asStateFlow()

  // Database Data Streams
  val assetDossier: StateFlow<AssetDossier?> = repository.assetDossier
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null
    )

  val allLogs: StateFlow<List<DossierLog>> = repository.allLogs
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  init {
    viewModelScope.launch {
      repository.checkAndPrepopulate()
    }
  }

  // Auth Action
  fun login(name: String, clearance: String): Boolean {
    if (name.isNotBlank() && clearance.isNotBlank()) {
      _currentUsername.value = name.trim()
      _currentClearance.value = clearance.trim()
      _isLoggedIn.value = true

      // Customize dossier custodian to logged in user if desired (Personalization!)
      viewModelScope.launch {
        assetDossier.value?.let { currentDossier ->
          repository.updateAsset(
            currentDossier.copy(
              custodianName = name.trim(),
              uid = "UID-${clearance.trim().uppercase()}"
            )
          )
        }
      }
      return true
    }
    return false
  }

  fun logout() {
    _isLoggedIn.value = false
    _currentUsername.value = null
    _currentClearance.value = null
    _currentTab.value = "ASSETS"
  }

  fun toggleDarkMode() {
    _isDarkMode.value = !_isDarkMode.value
  }

  fun selectTab(tab: String) {
    _currentTab.value = tab
  }

  // State Modifying Actions
  fun updateDossier(
    name: String,
    series: String,
    serial: String,
    classification: String,
    location: String
  ) {
    viewModelScope.launch {
      assetDossier.value?.let { currentDossier ->
        repository.updateAsset(
          currentDossier.copy(
            name = name,
            series = series,
            serialReference = serial,
            classification = classification,
            lastLocation = location
          )
        )
      }
    }
  }

  fun addNewLog(category: String, notes: String) {
    viewModelScope.launch {
      val logNo = "LOG #${(8000..8999).random()}"
      val author = _currentUsername.value?.uppercase() ?: "ADMIN_R02"
      val isCritical = category.uppercase() == "CRITICAL" || category.uppercase() == "MAINTENANCE"

      repository.addLog(
        DossierLog(
          logNumber = logNo,
          category = category.uppercase(),
          dateStr = "22 MAY 2026", // Today's simulated date
          notes = notes,
          author = author,
          isCritical = isCritical
        )
      )
    }
  }

  fun toggleAssetStatus() {
    viewModelScope.launch {
      assetDossier.value?.let { currentDossier ->
        val newStatus = when (currentDossier.status) {
          "MAINTENANCE REQUIRED" -> "SECURED"
          "SECURED" -> "MISSING"
          else -> "MAINTENANCE REQUIRED"
        }
        
        repository.updateAsset(currentDossier.copy(status = newStatus))
        
        val categoryLog = if (newStatus == "MAINTENANCE REQUIRED") "MAINTENANCE" else "GENERAL"
        val notesLog = when (newStatus) {
          "SECURED" -> "Status manually overwritten of asset to SECURED state. Field sensors reporting green."
          "MISSING" -> "CRITICAL ALTERNATIVE ALERT: Asset reported MISSING by active monitoring agent."
          else -> "Status check performed. Flagged for scheduled maintenance calibration cycle."
        }

        repository.addLog(
          DossierLog(
            logNumber = "LOG #${(8000..8999).random()}",
            category = categoryLog,
            dateStr = "22 MAY 2026",
            notes = notesLog,
            author = _currentUsername.value?.uppercase() ?: "SYSTEM_AUTH"
          )
        )
      }
    }
  }

  fun scheduleInspection(dateStr: String) {
    viewModelScope.launch {
      assetDossier.value?.let { currentDossier ->
        val updatedDossier = currentDossier.copy(
          status = "MAINTENANCE REQUIRED"
        )
        repository.updateAsset(updatedDossier)

        repository.addLog(
          DossierLog(
            logNumber = "LOG #${(8000..8999).random()}",
            category = "MAINTENANCE",
            dateStr = dateStr,
            notes = "Scheduled active inspections cycle scheduled. Custody team set to examine precision optics housing units.",
            author = _currentUsername.value?.uppercase() ?: "SCHEDULER"
          )
        )
      }
    }
  }

  fun markAsMissing() {
    viewModelScope.launch {
      assetDossier.value?.let { currentDossier ->
        repository.updateAsset(
          currentDossier.copy(
            status = "MISSING"
          )
        )

        repository.addLog(
          DossierLog(
            logNumber = "LOG #${(8000..8999).random()}",
            category = "CRITICAL",
            dateStr = "22 MAY 2026",
            notes = "ALERT EVENT LOGGED: Operational personnel reported device location trace failure. Asset key marked as MISSING. Active tracker sweeps initiated.",
            author = _currentUsername.value?.uppercase() ?: "TACTICAL",
            isCritical = true
          )
        )
      }
    }
  }
}
