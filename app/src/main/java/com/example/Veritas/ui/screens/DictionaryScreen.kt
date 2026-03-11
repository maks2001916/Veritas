package com.example.Veritas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.Veritas.core.Dictionary
import com.example.Veritas.viewmodel.DictionaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel,
    onDictionarySelected: (Dictionary) -> Unit,
    onCreateDictionary: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 Словари", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateDictionary) {
                Icon(Icons.Default.Add, contentDescription = "Создать словарь")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.dictionaries) { dictionary ->
                    DictionaryItem(
                        dictionary = dictionary,
                        isSelected = dictionary.id == state.selectedDictionary?.id,
                        onSelect = { onDictionarySelected(dictionary) },
                        onDelete = { viewModel.deleteDictionary(dictionary.id) }
                    )
                }
            }
        }
    }
}



@Composable
private fun DictionaryItem(
    dictionary: Dictionary,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dictionary.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    if (dictionary.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("default", fontSize = 10.sp) },
                            enabled = false
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Алфавит: ${dictionary.alphabet.take(30)}${if (dictionary.alphabet.length > 30) "..." else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Text(
                    "📄 ${dictionary.lengthOfPage} симв. | 🔤 ${dictionary.alphabet.length} знаков",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (!dictionary.isDefault) {
                IconButton(onClick = { onDelete() }) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DictionaryItemSelectedView() {
    DictionaryItem(
        dictionary = Dictionary.RUSSIAN,
        isSelected = true,
        onSelect = {},
        onDelete = {}
    )
}

@Preview(showBackground = true)
@Composable
fun DictionaryItemUnselectedView() {
    DictionaryItem(
        dictionary = Dictionary.RUSSIAN,
        isSelected = false,
        onSelect = {},
        onDelete = {}
    )
}
