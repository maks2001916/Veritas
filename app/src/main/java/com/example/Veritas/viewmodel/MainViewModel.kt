package com.example.Veritas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Veritas.core.Dictionary
import com.example.Veritas.core.DictionaryManager
import com.example.Veritas.core.LibraryOfBabelCore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val dictionaryManager: DictionaryManager,
    private val libraryCore: LibraryOfBabelCore = LibraryOfBabelCore()
) : ViewModel() {

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _dictionaryState = MutableStateFlow(DictionaryUiState())
    val dictionaryState: StateFlow<DictionaryUiState> = _dictionaryState.asStateFlow()

    private val _navigation = MutableSharedFlow<NavigationEvent>()
    val navigation: SharedFlow<NavigationEvent> = _navigation.asSharedFlow()





    init {
        loadDictionaries()
    }

    fun loadDictionaries() {
        viewModelScope.launch {
            _dictionaryState.update { it.copy(isLoading = true) }
            try {
                val dicts = dictionaryManager.getAllDictionaries()
                val selected = dictionaryManager.getSelectedDictionary()
                _dictionaryState.update {
                    it.copy(
                        dictionaries = dicts,
                        selectedDictionary = selected,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _dictionaryState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun selectDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            dictionaryManager.setSelectedDictionary(dictionary.id)
            _dictionaryState.update { it.copy(selectedDictionary = dictionary) }

        }
    }

    fun updateQuery(query: String) {
        _searchState.update { it.copy(query = query) }
    }

    fun toggleRegexMode(enabled: Boolean) {
        _searchState.update { it.copy(regexMode = enabled) }
    }

    fun updateRegexPattern(pattern: String) {
        _searchState.update { it.copy(regexPattern = pattern) }
    }

    fun search() {
        viewModelScope.launch {
            val state = _searchState.value

            if (state.regexMode) {
                searchByRegex(state.regexPattern)
            } else {
                searchByText(state.query)
            }
        }
    }

    private fun searchByText(text: String) {
        viewModelScope.launch {
            val dict = _dictionaryState.value.selectedDictionary
            if (dict == null) {
                _searchState.update {
                    it.copy(isSearching = false, uiState = UiState.Error("Выберите словарь"))
                }
                return@launch
            }
            _searchState.update { it.copy(isSearching = true, uiState = UiState.Loading) }

            try {
                // Собираем до 10 результатов, между которыми можно переключаться
                var results: MutableList<LibraryOfBabelCore.SearchResult> =
                    mutableListOf()
                repeat(10) {
                    val singleResult = libraryCore.search(text, customDictionary = dict)

                    if (singleResult.isSuccess) {
                        singleResult.getOrNull()?.let { results.add(it) }
                    }

                }

                if (results.isEmpty()) {
                    _searchState.update {
                        it.copy(isSearching = false, uiState = UiState.Error("Ничего не найдено"))
                    }
                } else {
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            uiState = UiState.Success(results = results, currentPage = 0)
                        )
                    }
                    _navigation.emit(NavigationEvent.NavigateToResult(results.first().address))
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(isSearching = false, uiState = UiState.Error(e.message ?: "Неизвестная ошибка"))
                }
            }
        }
    }

    private fun searchByRegex(pattern: String) {
        viewModelScope.launch {
            val dict = _dictionaryState.value.selectedDictionary
            if (dict == null) {
                _searchState.update {
                    it.copy(isSearching = false, uiState = UiState.Error("Выберите словарь"))
                }
                return@launch
            }
            _searchState.update { it.copy(isSearching = true, uiState = UiState.Loading) }

            try {
                val result = libraryCore.searchByRegex(
                    regex = pattern,
                    customDictionary = dict,
                    maxAttempts = 50000,
                    onProgress = { attempt ->
                        if (attempt % 1000 == 0) {
                            _searchState.update {
                                it.copy(searchProgress = attempt to 50000)
                            }
                        }
                    }
                )

                result.onSuccess { searchResult ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            uiState = if (searchResult != null)
                                UiState.Success(results = listOf(searchResult), currentPage = 0)
                            else
                                UiState.Error("Ничего не найдено")
                        )
                    }
                    searchResult?.let {
                        _navigation.emit(NavigationEvent.NavigateToResult(it.address))
                    }
                }.onFailure { error ->
                    _searchState.update {
                        it.copy(isSearching = false, uiState = UiState.Error(error.message ?: "Ошибка"))
                    }
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(isSearching = false, uiState = UiState.Error(e.message ?: "Неизвестная ошибка"))
                }
            }
        }
    }

    fun getPageByAddress(address: String) {
        viewModelScope.launch {
            val dict = _dictionaryState.value.selectedDictionary
            _searchState.update { it.copy(isSearching = true, uiState = UiState.Loading) }

            try {
                val result = libraryCore.getPageByAddress(address, dict)
                result.onSuccess { searchResult ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            uiState = UiState.Success(results = listOf(searchResult), currentPage = 0)
                        )
                    }
                }.onFailure { error ->
                    _searchState.update {
                        it.copy(isSearching = false, uiState = UiState.Error(error.message ?: "Ошибка"))
                    }
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(isSearching = false, uiState = UiState.Error(e.message ?: "Неизвестная ошибка"))
                }
            }
        }
    }

    /** Загружает страницу по адресу в состояние поиска (для экрана Result). */
    fun getResultByAddress(address: String) {
        getPageByAddress(address)
    }

    fun clearResult() {
        _searchState.update { it.copy(uiState = UiState.Idle) }
    }

    /** Переключение между найденными результатами */
    fun nextResult() {
        _searchState.update { state ->
            val ui = state.uiState
            if (ui is UiState.Success && ui.results.isNotEmpty()) {
                val newIndex = (ui.currentPage + 1).coerceAtMost(ui.results.lastIndex)
                state.copy(uiState = ui.copy(currentPage = newIndex))
            } else {
                state
            }
        }
    }

    fun previousResult() {
        _searchState.update { state ->
            val ui = state.uiState
            if (ui is UiState.Success && ui.results.isNotEmpty()) {
                val newIndex = (ui.currentPage - 1).coerceAtLeast(0)
                state.copy(uiState = ui.copy(currentPage = newIndex))
            } else {
                state
            }
        }
    }

    fun createDictionary(name: String, alphabet: String): Result<Dictionary> {
        return dictionaryManager.saveDictionary(
            Dictionary(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                alphabet = alphabet
            )
        ).onSuccess { loadDictionaries() }
    }

    fun deleteDictionary(dictionaryId: String) {
        dictionaryManager.deleteDictionary(dictionaryId)
        loadDictionaries()
    }
}

sealed class NavigationEvent {
    data class NavigateToResult(val address: String) : NavigationEvent()
    data class NavigateToDictionary(val dictionaryId: String) : NavigationEvent()
    object NavigateBack : NavigationEvent()
}