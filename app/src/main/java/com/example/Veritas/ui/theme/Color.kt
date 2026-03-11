package com.example.Veritas.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ==================== БАЗОВЫЕ ЦВЕТА ====================
val BabylonPurple = Color(0xFF6B46C1)
val BabylonPurpleLight = Color(0xFF8B6FD9)
val BabylonPurpleDark = Color(0xFF553C9A)
val BabylonGold = Color(0xFFFFD700)
val BabylonGoldLight = Color(0xFFFFE44D)
val BabylonGoldDark = Color(0xFFB8860B)

// ==================== СВЕТЛАЯ ТЕМА ====================
val BackgroundLight = Color(0xFFF8F5F0)  // Цвет пергамента
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF1A1A2E)       // Тёмно-синий для текста
val TextSecondaryLight = Color(0xFF666666)     // Серый для вторичного текста

// ==================== ТЁМНАЯ ТЕМА ====================

val BackgroundDark = Color(0xFF1A1A2E)         // Тёмно-синий фон
val SurfaceDark = Color(0xFF16213E)            // Чуть светлее для карточек
val TextPrimaryDark = Color(0xFFF8F5F0)        // Светлый текст
val TextSecondaryDark = Color(0xFFB0B0B0)      // Серый для вторичного текста

// ==================== ФУНКЦИОНАЛЬНЫЕ ЦВЕТА ====================

val ErrorColor = Color(0xFFDC3545)             // Красный для ошибок
val ErrorColorLight = Color(0xFFF5C6CB)
val ErrorColorDark = Color(0xFF721C24)

val SuccessColor = Color(0xFF28A745)           // Зелёный для успеха
val SuccessColorLight = Color(0xFFD4EDDA)
val SuccessColorDark = Color(0xFF155724)

// ==================== ЦВЕТА ДЛЯ SCHEME ====================

// Для Material 3 ColorScheme нужны эти цвета
val PrimaryLight = BabylonPurple
val OnPrimaryLight = Color.White
val PrimaryContainerLight = BabylonPurpleLight
val OnPrimaryContainerLight = BabylonPurpleDark

val PrimaryDark = BabylonPurpleLight
val OnPrimaryDark = Color.Black
val PrimaryContainerDark = BabylonPurpleDark
val OnPrimaryContainerDark = BabylonPurpleLight

val SecondaryLight = BabylonGold
val OnSecondaryLight = Color.Black
val SecondaryContainerLight = BabylonGoldLight
val OnSecondaryContainerLight = BabylonGoldDark

val SecondaryDark = BabylonGold
val OnSecondaryDark = Color.Black
val SecondaryContainerDark = BabylonGoldDark
val OnSecondaryContainerDark = BabylonGoldLight

val BackgroundLightColor = BackgroundLight
val OnBackgroundLight = TextPrimaryLight

val BackgroundDarkColor = BackgroundDark
val OnBackgroundDark = TextPrimaryDark

val SurfaceLightColor = SurfaceLight
val OnSurfaceLight = TextPrimaryLight

val SurfaceDarkColor = SurfaceDark
val OnSurfaceDark = TextPrimaryDark

val ErrorLight = ErrorColor
val OnErrorLight = Color.White
val ErrorContainerLight = ErrorColorLight
val OnErrorContainerLight = ErrorColorDark

val ErrorDark = ErrorColor
val OnErrorDark = Color.White
val ErrorContainerDark = ErrorColorDark
val OnErrorContainerDark = ErrorColorLight