package com.example.Veritas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Veritas.core.Dictionary
import com.example.Veritas.core.DictionaryManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DictionaryUiState(
    val dictionaries: List<Dictionary> = emptyList(),
    val selectedDictionary: Dictionary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastAction: DictionaryAction? = null
)

sealed class DictionaryAction {
    object DictionaryCreated : DictionaryAction()
    object DictionaryDeleted : DictionaryAction()
    object DictionarySelected : DictionaryAction()
    data class Error(val message: String) : DictionaryAction()
}

class DictionaryViewModel(
    private val dictionaryManager: DictionaryManager
) : ViewModel() {

    private val _state = MutableStateFlow(DictionaryUiState())
    val state: StateFlow<DictionaryUiState> = _state.asStateFlow()

    init {
        loadDictionaries()
    }

    fun loadDictionaries() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val dicts = dictionaryManager.getAllDictionaries()
                val selected = dictionaryManager.getSelectedDictionary()
                _state.update {
                    it.copy(
                        dictionaries = dicts,
                        selectedDictionary = selected,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        lastAction = DictionaryAction.Error(e.message ?: "Ошибка загрузки")
                    )
                }
            }
        }
    }

    fun selectDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            try {
                dictionaryManager.setSelectedDictionary(dictionary.id)
                _state.update {
                    it.copy(
                        selectedDictionary = dictionary,
                        lastAction = DictionaryAction.DictionarySelected
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message,
                        lastAction = DictionaryAction.Error(e.message ?: "Ошибка выбора")
                    )
                }
            }
        }
    }

    fun createDictionary(
        name: String,
        alphabet: String,
        digs: String = "0123456789abcdefghijklmnopqrstuvwxyz",
        lengthOfPage: Int = 4819,
        lengthOfTitle: Int = 31
    ): Result<Dictionary> {
        val dictionary = Dictionary(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            alphabet = alphabet,
            digs = digs,
            lengthOfPage = lengthOfPage,
            lengthOfTitle = lengthOfTitle
        )
        return saveDictionary(dictionary)
    }

    /** Сохраняет готовый словарь (например, из CreateDictionaryScreen). */
    fun saveDictionary(dictionary: Dictionary): Result<Dictionary> {
        val validation = dictionary.validate()
        if (!validation.isValid) {
            return Result.failure(Exception(validation.errors.joinToString("\n")))
        }
        return dictionaryManager.saveDictionary(dictionary)
            .onSuccess {
                _state.update {
                    it.copy(
                        dictionaries = it.dictionaries + dictionary,
                        lastAction = DictionaryAction.DictionaryCreated
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message,
                        lastAction = DictionaryAction.Error(error.message ?: "Ошибка создания")
                    )
                }
            }
    }

    fun deleteDictionary(dictionaryId: String) {
        viewModelScope.launch {
            try {
                val deleted = dictionaryManager.deleteDictionary(dictionaryId)
                if (deleted) {
                    loadDictionaries() // Перезагружаем список
                    _state.update {
                        it.copy(lastAction = DictionaryAction.DictionaryDeleted)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message,
                        lastAction = DictionaryAction.Error(e.message ?: "Ошибка удаления")
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearLastAction() {
        _state.update { it.copy(lastAction = null) }
    }
}