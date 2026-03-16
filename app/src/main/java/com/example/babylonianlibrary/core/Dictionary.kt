package com.example.babylonianlibrary.core

import java.io.Serializable

data class Dictionary(
    val id: String,
    val name: String,
    val alphabet: String,
    val digs: String = "0123456789abcdefghijklmnopqrstuvwxyz",
    val lengthOfPage: Int = 4819,
    val lengthOfTitle: Int = 31,
    val walls: Int = 5,
    val shelves: Int = 7,
    val volumes: Int = 31,
    val pages: Int = 421,
    val createAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
): Serializable {
    companion object {
        val RUSSIAN = Dictionary(
            id = "ru_default",
            name = "Русский (по умолчанию)",
            alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя, .",
            isDefault = true
        )

        val ENGLISH = Dictionary(
            id = "en_default",
            name = "English",
            alphabet = "abcdefghijklmnopqrstuvwxyz, .",
            isDefault = false
        )

        val EXTENDED = Dictionary(
            id = "extended",
            name = "Расширенный (RU+EN+0-9)",
            alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz0123456789, .!?",
            isDefault = false
        )

        val BINARY = Dictionary(
            id = "binary",
            name = "Бинарный",
            alphabet = "01 ",
            lengthOfPage = 3200,
            isDefault = false
        )
        val EMOJI = Dictionary(
            id = "emoji",
            name = "Эмодзи",
            alphabet = "😀😃😄😁😆😅😂🤣😊😇🙂🙃😉😌😍🥰😘😗😙😚😋😛😝😜🤪🤨🧐🤓😎🤩🥳😏😒😞😔😟😕🙁☹️😣😖😫😩🥺😢😭😤😠😡🤬🤯😳🥵🥶😱😨😰😥😓🤗🤔🤭🤫🤥😶😐😑😬🙄😯😦😧😮😲🥱😴🤤😪😵🤐🥴🤢🤮🤧😷🤒🤕🤑🤠😈👿👹👺🤡💩👻💀☠️👽👾🤖🎃😺😸😹😻😼😽🙀😿😾, .",
            lengthOfPage = 2000,
            isDefault = false
        )

        fun getBuiltIn() = listOf(RUSSIAN, ENGLISH, EXTENDED, BINARY, EMOJI)
    }

    fun containsChar(char: Char): Boolean = char in alphabet
    fun containsString(str: String): Boolean = str.all {it in alphabet}



    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (alphabet.isEmpty()) errors.add("Алфавит не может быть пустым")
        if (alphabet.length < 2) errors.add("Алфавит должен содержать минимум 2 символа")
        if (digs.isEmpty()) errors.add("Digs не может быть пустым")
        if (digs.length < 2) errors.add("Digs должен содержать минимум 2 символа")
        if (alphabet.any { it in digs }) errors.add("Алфавит и digs не должны пересекаться")
        if (lengthOfPage < 100) errors.add("Длина страницы слишком маленькая")
        if (lengthOfTitle < 1) errors.add("Длина заголовка слишком маленькая")

        return ValidationResult(errors.isEmpty(), errors)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}