package com.example.babylonianlibrary.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.babylonianlibrary.core.Dictionary
import com.example.babylonianlibrary.core.LibraryOfBabelCore
import com.example.babylonianlibrary.viewmodel.MainViewModel
import com.example.babylonianlibrary.viewmodel.SearchUiState
import com.example.babylonianlibrary.viewmodel.UiState


@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToResult: (String) -> Unit,
    onNavigateToDictionaries: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val dictionaryState by viewModel.dictionaryState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HomeTopBar(
                selectedDictionary = dictionaryState.selectedDictionary,
                onDictionaryClick = onNavigateToDictionaries,
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            if (searchState.uiState is UiState.Success) {
                FloatingActionButton(onClick = viewModel::clearResult) {
                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 🔍 Поисковая секция
            SearchSection(
                state = searchState,
                onQueryChange = viewModel::updateQuery,
                onRegexToggle = viewModel::toggleRegexMode,
                onRegexPatternChange = viewModel::updateRegexPattern,
                onSearch = viewModel::search,
                isSearching = searchState.isSearching
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 📊 Результаты
            when (val uiState = searchState.uiState) {
                is UiState.Idle -> {
                    EmptyState()
                }
                is UiState.Loading -> {
                    LoadingState(progress = searchState.searchProgress)
                }
                is UiState.Success -> {
                    val current = uiState.results.getOrNull(uiState.currentPage)
                    if (current != null) {
                        // Информация о текущем результате и навигация к подробному экрану
                        Text(
                            text = "Результат ${uiState.currentPage + 1} из ${uiState.results.size}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ResultPreview(
                            result = current,
                            onClick = { onNavigateToResult(current.address) }
                        )
                        if (uiState.results.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = viewModel::previousResult,
                                    enabled = uiState.currentPage > 0
                                ) {
                                    Text("◀ Предыдущий")
                                }
                                TextButton(
                                    onClick = viewModel::nextResult,
                                    enabled = uiState.currentPage < uiState.results.lastIndex
                                ) {
                                    Text("Следующий ▶")
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorState(message = uiState.message, onRetry = viewModel::search)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    selectedDictionary: Dictionary?,
    onDictionaryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "📚 Библиотека Вавилона",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        actions = {
            // Выбранный словарь
            IconButton(
                onClick = onDictionaryClick, modifier = Modifier
                    .wrapContentWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Словари"
                    )
                    selectedDictionary?.name?.let {
                        Text(it, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }
        }
    )
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, )
@Composable
fun HomeTopBarPreview() {
    HomeTopBar(
        selectedDictionary = Dictionary.RUSSIAN,
        onSettingsClick = {},
        onDictionaryClick = {}
    )
}

@Composable
private fun SearchSection(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRegexToggle: (Boolean) -> Unit,
    onRegexPatternChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Переключатель режима
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Режим поиска", fontWeight = FontWeight.Medium)
                Switch(
                    checked = state.regexMode,
                    onCheckedChange = onRegexToggle,
                    enabled = !isSearching
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Поле ввода
            if (state.regexMode) {
                OutlinedTextField(
                    value = state.regexPattern,
                    onValueChange = onRegexPatternChange,
                    label = { Text("Регулярное выражение") },
                    placeholder = { Text("например: привет.*мир") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSearching,
                    singleLine = true,
                    isError = state.regexPattern.isNotEmpty() && !isValidRegex(state.regexPattern)
                )
            } else {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    label = { Text("Текст для поиска") },
                    placeholder = { Text("Введите фразу...") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSearching,
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка поиска
            Button(
                onClick = onSearch,
                enabled = !isSearching && (state.query.isNotEmpty() || state.regexPattern.isNotEmpty()),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSearching) "Поиск..." else "🔍 Найти в библиотеке")
            }

            // Прогресс для regex-поиска
            if (isSearching && state.regexMode && state.searchProgress != null) {
                LinearProgressIndicator(
                    progress = { state.searchProgress.first.toFloat() / state.searchProgress.second },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Text(
                    "Попыток: ${state.searchProgress.first}/${state.searchProgress.second}",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchSectionPreview() {
    val state = SearchUiState(
        query = " быть или не быть",
        uiState = UiState.Idle,
        isSearching = false
    )
    SearchSection(
        state = state,
        onQueryChange = {},
        onSearch = {},
        onRegexPatternChange = {},
        onRegexToggle = {},
        isSearching = false
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🗝️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Введите текст для поиска\nв бесконечной библиотеке",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        AssistChip(
            onClick = { /* Показать пример */ },
            label = { Text("💡 Пример: «быть или не быть»") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    ErrorState(
        message = "string",
        onRetry = {}
    )
}

@Composable
private fun LoadingState(progress: Pair<Int, Int>?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ищем в бесконечности...")

        progress?.let { (current, total) ->
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { current.toFloat() / total },
                modifier = Modifier.width(200.dp)
            )
            Text("$current/$total попыток", fontSize = 12.sp)
        }
    }
}


@Composable
private fun ResultPreview(
    result: LibraryOfBabelCore.SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Адрес
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📍 ${result.address}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(Icons.Default.OpenInNew, contentDescription = "Открыть", modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Заголовок
            Text(
                "📄 ${result.title.trim()}",
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Предпросмотр контента
            Text(
                result.content.take(200).trim() + if (result.content.length > 200) "..." else "",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                maxLines = 5,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Мета-информация
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("📖 ${result.content.length} символов", fontSize = 12.sp)
                Text("🔤 ${result.dictionaryId}", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ошибка: $message",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("Повторить", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

// Простая валидация regex
private fun isValidRegex(pattern: String): Boolean {
    return try {
        Regex(pattern)
        true
    } catch (e: Exception) {
        false
    }
}