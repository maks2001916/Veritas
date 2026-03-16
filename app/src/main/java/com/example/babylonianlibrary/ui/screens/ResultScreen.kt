package com.example.babylonianlibrary.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.babylonianlibrary.core.LibraryOfBabelCore
import com.example.babylonianlibrary.viewmodel.MainViewModel
import com.example.babylonianlibrary.viewmodel.UiState

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    address: String,
    onBack: () -> Unit,
    onCopyAddress: () -> Unit,
    onShare: (shareText: String) -> Unit
) {
    val context = LocalContext.current
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    LaunchedEffect(address) {
        // Если результат с таким адресом уже есть в списке, не перезаписываем состояние
        val currentState = searchState.uiState
        val hasAddress = (currentState as? UiState.Success)
            ?.results
            ?.any { it.address == address } == true
        if (!hasAddress) {
            viewModel.getResultByAddress(address)
        }
    }

    when (val uiState = searchState.uiState) {
        is UiState.Loading -> {
            ResultLoadingScreen(onBack = onBack)
        }
        is UiState.Success -> {
            val current = uiState.results.getOrNull(uiState.currentPage)
            if (current != null) {
                ResultContentScreen(
                    result = current,
                    onBack = onBack,
                    onCopyAddress = {
                        copyAddressToClipboard(context, current.address)
                        onCopyAddress()
                    },
                    onShare = {
                        val shareText = buildShareText(current)
                        onShare(shareText)
                    },
                    onNext = viewModel::nextResult,
                    onPrevious = viewModel::previousResult,
                    currentIndex = uiState.currentPage,
                    totalCount = uiState.results.size
                )
            } else {
                ResultLoadingScreen(onBack = onBack)
            }
        }
        is UiState.Error -> {
            ResultErrorScreen(
                message = uiState.message,
                onBack = onBack,
                onRetry = { viewModel.getResultByAddress(address) }
            )
        }
        else -> ResultLoadingScreen(onBack = onBack)
    }
}

private fun copyAddressToClipboard(context: Context, address: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("address", address))
}

private fun buildShareText(result: LibraryOfBabelCore.SearchResult): String {
    return "${result.title}\n\n${result.content}\n\nАдрес: ${result.address}"
}

@Composable
private fun ResultContentScreen(
    result: LibraryOfBabelCore.SearchResult,
    onBack: () -> Unit,
    onCopyAddress: () -> Unit,
    onShare: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    currentIndex: Int,
    totalCount: Int
) {
    var addressExpanded by remember { mutableStateOf(true) }
    var titleExpanded by remember { mutableStateOf(true) }
    var contentExpanded by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            ResultTopBar(
                onBack = onBack,
                onCopyAddress = onCopyAddress,
                onShare = onShare
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            AddressSection(
                address = result.address,
                expanded = addressExpanded,
                onToggle = { addressExpanded = !addressExpanded }
            )
            TitleSection(
                title = result.title,
                expanded = titleExpanded,
                onToggle = { titleExpanded = !titleExpanded }
            )
            ContentSection(
                content = result.content,
                expanded = contentExpanded,
                onToggle = { contentExpanded = !contentExpanded }
            )
            MetaSection(result = result)
            if (totalCount > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onPrevious,
                        enabled = currentIndex > 0
                    ) {
                        Text("◀ Предыдущий")
                    }
                    Text(
                        text = "${currentIndex + 1} / $totalCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(
                        onClick = onNext,
                        enabled = currentIndex < totalCount - 1
                    ) {
                        Text("Следующий ▶")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultContentScreenPreview() {
    val coords = LibraryOfBabelCore.Coordinates(
        wall = 1,
        shelf = 3,
        volume = 7,
        page = 42
    )
    val result = LibraryOfBabelCore.SearchResult(
        coordinates = coords,
        hex = "abc123",
        title = "Глава первая",
        content = "Текст страницы...",
        address = "abc123-1-3-07-042",
        dictionaryId = "ru_default"
    )
    ResultContentScreen(
        result = result,
        onShare = {},
        onBack = {},
        onCopyAddress = {},
        onNext = {},
        onPrevious = {},
        currentIndex = 0,
        totalCount = 3
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultLoadingScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Страница", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Загрузка страницы...")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultLoadingScreenPreview() {
    ResultLoadingScreen(onBack = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultErrorScreen(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Страница", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ошибка: $message", color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultErrorScreenPreview() {
    ResultErrorScreen(
        "string",
        {},
        {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultTopBar(
    onBack: () -> Unit,
    onCopyAddress: () -> Unit,
    onShare: () -> Unit
) {
    TopAppBar(
        title = { Text("Страница", fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
        },
        actions = {
            IconButton(onClick = onCopyAddress) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Копировать адрес")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Поделиться")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ResultTopBarPreview() {
    ResultTopBar(
        onBack = {},
        onCopyAddress = {},
        onShare = {}
    )
}

@Composable
private fun AddressSection(
    address: String,
    expanded: Boolean = true,
    onToggle: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍 Адрес страницы", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть"
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    SelectionContainer {
                        Text(
                            address,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddressSectionView() {
    AddressSection(address = "abc123-1-3-07-042")
}

@Composable
private fun TitleSection(
    title: String,
    expanded: Boolean = true,
    onToggle: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onToggle() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📄 Заголовок", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть"
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            title.ifBlank { "〈пусто〉" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TitleSectionPreview() {
    TitleSection("строка")
}
@Composable
private fun ContentSection(
    content: String,
    expanded: Boolean = true,
    onToggle: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onToggle() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📖 Содержимое", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${content.length} символов", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть"
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            content,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContentSectionPreview() {
    ContentSection("Строка")
}

@Composable
private fun MetaSection(result: LibraryOfBabelCore.SearchResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        MetaItem("🧱 Стена", result.coordinates.wall.toString())
        MetaItem("📚 Полка", result.coordinates.shelf.toString())
        MetaItem("📕 Том", result.coordinates.volume.toString())
        MetaItem("📃 Страница", result.coordinates.page.toString())
    }
}


@Composable
private fun MetaItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun MetaSectionPreview() {
    val coords = LibraryOfBabelCore.Coordinates(
        wall = 1,
        shelf = 3,
        volume = 7,
        page = 42
    )
    val result = LibraryOfBabelCore.SearchResult(
        coordinates = coords,
        hex = "abc123",
        title = "Глава первая",
        content = "Текст страницы...",
        address = "abc123-1-3-07-042",
        dictionaryId = "ru_default"
    )
    MetaSection(result = result)
}
