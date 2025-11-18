package com.Rajath.aura.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Rajath.aura.data.JournalEntry
import com.Rajath.aura.data.JournalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface JournalUiState {
    object Idle : JournalUiState
    object Loading : JournalUiState
    data class Success(val sentiment: String, val score: Float) : JournalUiState
    data class Error(val message: String) : JournalUiState
}

sealed class JournalEvent {
    data class Saved(val id: String) : JournalEvent()
    data class Error(val message: String) : JournalEvent()
}

class JournalViewModel(
    private val repo: JournalRepository = JournalRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<JournalUiState>(JournalUiState.Idle)
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    // one-off events (snackbar / navigation)
    private val _events = MutableSharedFlow<JournalEvent>()
    val events = _events.asSharedFlow()

    // recent journals state (observed from Firestore)
    private val _recentJournals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val recentJournals: StateFlow<List<JournalEntry>> = _recentJournals.asStateFlow()

    // keep reference to the current observe job so we can cancel/resubscribe safely
    private var observeJob: Job? = null

    // --- analyze and save (unchanged behaviour) ---
    fun analyzeAndSave(userId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.value = JournalUiState.Loading
            try {
                val (label, score) = repo.analyzeText(text)
                _uiState.value = JournalUiState.Success(label, score)

                // attempt save
                val res = repo.saveJournal(userId, text, label, score)
                if (res.isSuccess) {
                    _events.emit(JournalEvent.Saved(res.getOrNull() ?: ""))
                } else {
                    _events.emit(JournalEvent.Error(res.exceptionOrNull()?.localizedMessage ?: "Save failed"))
                }
            } catch (t: Throwable) {
                _uiState.value = JournalUiState.Error(t.localizedMessage ?: "Unknown error")
                _events.emit(JournalEvent.Error(t.localizedMessage ?: t.javaClass.simpleName))
            }
        }
    }

    fun reset() {
        _uiState.value = JournalUiState.Idle
    }

    // --- observe recent journals (realtime) ---
    fun observeRecent(userId: String) {
        // cancel any previous subscription for safety
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            repo.getRecentJournals(userId)
                .catch { e ->
                    Log.w("AURA-HF", "Error observing recent journals: ${e.localizedMessage}")
                    // optional: emit empty list to UI on error
                    _recentJournals.value = emptyList()
                }
                .collect { list ->
                    // Defensive mapping: ensure timestamp is non-zero (fallback to now if missing)
                    val normalized = list.map { entry ->
                        if (entry.timestamp > 0L) entry
                        else entry.copy(timestamp = System.currentTimeMillis())
                    }

                    // Sort newest first for lists; analytics can sort ascending when needed
                    val sortedDesc = normalized.sortedByDescending { it.timestamp }

                    // set state
                    _recentJournals.value = sortedDesc

                    // debug log so you can inspect timestamps in Logcat
                    Log.d("AURA-VM", "recentJournals size=${sortedDesc.size}")
                    sortedDesc.take(10).forEachIndexed { idx, e ->
                        Log.d(
                            "AURA-VM",
                            "[$idx] id=${e.id} ts=${e.timestamp} score=${e.score} sentiment='${e.sentiment}' text='${e.text.take(40)}'"
                        )
                    }
                }
        }
    }

    // --- delete a journal (listener will cause UI to update) ---
    fun deleteJournal(userId: String, id: String) {
        viewModelScope.launch {
            try {
                repo.deleteJournal(userId, id)
            } catch (t: Throwable) {
                _events.emit(JournalEvent.Error(t.localizedMessage ?: "Delete failed"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
