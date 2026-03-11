package com.example.Veritas.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.Veritas.core.LibraryOfBabelCore

// ==================== STRING EXTENSIONS ====================

/**
 * Обрезает строку до указанной длины с добавлением многоточия
 */
fun String.truncate(maxLength: Int, suffix: String = "…"): String {
    return if (length <= maxLength) this else take(maxLength - suffix.length) + suffix
}

/**
 * Проверяет, является ли строка валидным адресом библиотеки
 */
fun String.isValidLibraryAddress(): Boolean {
    val parts = split("-")
    return parts.size == 5 &&
            parts[0].isNotEmpty() && // hex
            parts.drop(1).all { it.toIntOrNull() != null } // координаты
}

// ==================== COLOR EXTENSIONS ====================

/**
 * Конвертирует hex-цвет в Compose Color
 */
fun String.toComposeColor(): Color? {
    return try {
        val hex = this.replace("#", "")
        when (hex.length) {
            6 -> Color(
                red = hex.substring(0, 2).toInt(16),
                green = hex.substring(2, 4).toInt(16),
                blue = hex.substring(4, 6).toInt(16)
            )
            8 -> Color(
                alpha = hex.substring(0, 2).toInt(16),
                red = hex.substring(2, 4).toInt(16),
                green = hex.substring(4, 6).toInt(16),
                blue = hex.substring(6, 8).toInt(16)
            )
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

// ==================== ANNOTATED STRING ====================

/**
 * Подсвечивает найденный текст в строке
 */
fun String.highlightMatches(
    searchText: String,
    highlightColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        append(this@highlightMatches)
        val lowerText = this@highlightMatches.lowercase()
        val lowerSearch = searchText.lowercase()

        var startIndex = 0
        while (startIndex < length) {
            val index = lowerText.indexOf(lowerSearch, startIndex)
            if (index == -1) break

            withStyle(style = SpanStyle(background = highlightColor)) {
                append(this@highlightMatches.substring(index, index + searchText.length))
            }
            startIndex = index + searchText.length
        }
    }
}

// ==================== CONTEXT EXTENSIONS ====================

/**
 * Показывает Toast с сообщением
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Создаёт Intent для шеринга текста
 */
fun createShareIntent(text: String, subject: String = "Библиотека Вавилона"): Intent {
    return Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
}

// ==================== LIBRARY CORE EXTENSIONS ====================

/**
 * Форматирует адрес для отображения с переносами
 */
fun LibraryOfBabelCore.SearchResult.formattedAddress(): String {
    return buildString {
        append("🧱${coordinates.wall} ")
        append("📚${coordinates.shelf} ")
        append("📕${coordinates.volume.toString().padStart(2, '0')} ")
        append("📃${coordinates.page.toString().padStart(3, '0')}")
    }
}

/**
 * Получает превью контента с выделением поискового запроса
 */
fun LibraryOfBabelCore.SearchResult.getContentPreview(
    maxLength: Int = 200,
    highlightQuery: String? = null
): String {
    val preview = content.take(maxLength).trim()
    return if (highlightQuery != null && preview.contains(highlightQuery, ignoreCase = true)) {
        val regex = Regex(Regex.escape(highlightQuery), RegexOption.IGNORE_CASE)
        preview.replace(regex) { "«${it.value}»" }
    } else {
        preview
    } + if (content.length > maxLength) "…" else ""
}

// ==================== NUMBER FORMATTING ====================

/**
 * Форматирует число с разделителями тысяч
 */
fun Int.formatWithSeparators(): String {
    return toString().reversed().chunked(3).joinToString(" ").reversed()
}

/**
 * Конвертирует размер в человекочитаемый формат
 */
fun Long.toHumanReadableSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> "%.1f KB".format(this / 1024.0)
        this < 1024 * 1024 * 1024 -> "%.1f MB".format(this / (1024.0 * 1024))
        else -> "%.1f GB".format(this / (1024.0 * 1024 * 1024))
    }
}