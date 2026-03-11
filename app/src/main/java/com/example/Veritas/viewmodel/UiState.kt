package com.example.Veritas.viewmodel

import com.example.Veritas.core.Dictionary
import com.example.Veritas.core.LibraryOfBabelCore

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(
        val results: List<LibraryOfBabelCore.SearchResult>,
        val currentPage: Int = 0,
        val totalResults: Int = 10
    ): UiState
    data class Error(val message: String) : UiState
}

data class SearchUiState(
    val query: String = "",
    val uiState: UiState = UiState.Idle,
    val selectedDictionary: Dictionary? = null,
    val isSearching: Boolean = false,
    val regexMode: Boolean = false,
    val regexPattern: String = "",
    val searchProgress: Pair<Int, Int>? = null
)