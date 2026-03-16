package com.example.babylonianlibrary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.babylonianlibrary.core.Dictionary
import com.example.babylonianlibrary.ui.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDictionaryScreen(
    onSave: (Dictionary) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var alphabet by remember { mutableStateOf("") }
    var digs by remember { mutableStateOf("0123456789abcdefghijklmnopqrstuvwxyz") }
    var lengthOfPage by remember { mutableStateOf("4819") }
    var lengthOfTitle by remember { mutableStateOf("31") }

    var showError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("✨ Новый словарь", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Название
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название словаря") },
                placeholder = { Text("например: «Мой алфавит»") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Алфавит
            OutlinedTextField(
                value = alphabet,
                onValueChange = { alphabet = it },
                label = { Text("Алфавит (символы книг)") },
                placeholder = { Text("абвгдеёжзийклмнопрстуфхцчшщъыьэюя, .") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            // Digs (опционально)
            OutlinedTextField(
                value = digs,
                onValueChange = { digs = it },
                label = { Text("Digs (символы адреса)") },
                placeholder = { Text("0123456789abcdefghijklmnopqrstuvwxyz") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                maxLines = 2,
                enabled = false // Можно оставить по умолчанию
            )

            // Настройки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = lengthOfPage,
                    onValueChange = { lengthOfPage = it },
                    label = { Text("Длина страницы") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = lengthOfTitle,
                    onValueChange = { lengthOfTitle = it },
                    label = { Text("Длина заголовка") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Подсказки
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("💡 Подсказки", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Алфавит должен содержать минимум 2 символа\n" +
                                "• Алфавит и digs не должны пересекаться\n" +
                                "• Длина страницы: минимум 100 символов",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Ошибка
            showError?.let { error ->
                ErrorBanner(
                    message = error,
                    onDismiss = { showError = null }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Отмена")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Валидация
                        when {
                            name.isBlank() -> showError = "Введите название словаря"
                            alphabet.length < 2 -> showError = "Алфавит должен содержать минимум 2 символа"
                            alphabet.any { it in digs } -> showError = "Алфавит и digs не должны пересекаться"
                            lengthOfPage.toIntOrNull()?.let { it < 100 } == true ->
                                showError = "Длина страницы должна быть не менее 100"
                            else -> {
                                val dictionary = Dictionary(
                                    id = "custom_${System.currentTimeMillis()}",
                                    name = name.trim(),
                                    alphabet = alphabet,
                                    digs = digs,
                                    lengthOfPage = lengthOfPage.toIntOrNull() ?: 4819,
                                    lengthOfTitle = lengthOfTitle.toIntOrNull() ?: 31
                                )
                                onSave(dictionary)
                            }
                        }
                    },
                    enabled = name.isNotBlank() && alphabet.length >= 2
                ) {
                    Text("💾 Сохранить")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateDictionaryScreenView() {
    CreateDictionaryScreen(
        onSave = {},
        onBack = {},
        modifier = Modifier
    )
}