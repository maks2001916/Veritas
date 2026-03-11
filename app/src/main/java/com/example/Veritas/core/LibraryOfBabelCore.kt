package com.example.Veritas.core

import java.security.MessageDigest
import kotlin.text.iterator
import kotlin.text.toInt

class LibraryOfBabelCore(private val dictionary: Dictionary = Dictionary.RUSSIAN) {

    private val alphabetMap = dictionary.alphabet.withIndex().associate { it.value to it.index }
    private val digsMap = dictionary.digs.withIndex().associate { it.value to it.index }


    data class Coordinates(
        val wall: Int, val shelf: Int,
        val volume: Int, val page: Int
    ) {
        fun toAddress(hex: String) =
            "$hex-$wall-$shelf-${volume.toString().padStart(2, '0')}-${page.toString().padStart(3, '0')}"
    }

    data class SearchResult(
        val  coordinates: Coordinates,
        val hex: String,
        val title: String,
        val content: String,
        val address: String,
        val dictionaryId: String
    )

    // Поиск по тексту
    fun search(
        searchStr: String,
        customDictionary: Dictionary
    ): Result<SearchResult> {
        val dict = customDictionary
        if (!dict.containsString(searchStr)) {
            val invalidChars = searchStr.filterNot { dict.containsChar(it) }
            return Result.failure(
                Exception("Текст содержит чимволы вне словаря: ${invalidChars.take(10)}")
            )
        }

        require(searchStr.isNotEmpty()) { "Поисковая строка не может быть пустой" }
        require(searchStr.length <= dict.lengthOfPage) {
            "Текст слишком длинный (макс. ${dictionary.lengthOfPage})"
        }

        val coordinates = generateRandomCoordinates(dict)
        val locHash = getLocationHash(coordinates.withPage())

        val depth = (Math.random() * (dictionary.lengthOfPage - searchStr.length)).toInt()
        val prefixedStr = buildString {
            repeat(depth) { append(dictionary.alphabet.random()) }
            append(searchStr)
        }

        val hex = transform(prefixedStr, locHash, encrypt = true)
        return Result.success(
            SearchResult(
                coordinates = coordinates,
                hex = hex,
                title = getTitle(coordinates, dict),
                content = formatContent(hex),
                address = coordinates.toAddress(hex),
                dictionaryId = dict.id
            )
        )
    }

    fun searchByRegex(
        regex: String,
        customDictionary: Dictionary,
        maxAttempts: Int = 10000,
        onProgress: ((attempt: Int) -> Unit)? = null
    ): Result<SearchResult?> {
        val dict  = customDictionary
        val pattern = try {
            Regex(regex)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid regex: ${e.message}" ,e)
        }

        repeat (maxAttempts) {attempt ->
            onProgress?.invoke(attempt + 1)
            val coords = generateRandomCoordinates(dict)
            val randomHex = generateRandomHex(100, dict)
            val address = coords.toAddress(randomHex)

            val pageResult = getPageByAddress(address, dict)
            if (pageResult.isSuccess) {
                val page = pageResult.getOrNull()
                if (page != null && pattern.containsMatchIn(page.content)) {
                    return Result.success(page)
                }
            }
        }

        return Result.success(null)
    }

    fun textExistsAtAddress(
        address: String,
        searchText: String,
        customDictionary: Dictionary
    ): Boolean {
        val dict = customDictionary ?: dictionary
        return try {
            val pageResult = getPageByAddress(address, dict)
            if (pageResult.isSuccess) {
                val page = pageResult.getOrNull()
                page?.content?.contains(searchText) == true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }

    }

    fun getPageByAddress(
        address: String,
        customDictionary: Dictionary? = null
    ): Result<SearchResult> {
        val dict = customDictionary ?: dictionary
        val parts = address.split("-")
        if (parts.size != 5) {
            return Result.failure(Exception("Неверный формат адреса")) }

        try {
            val hex = parts[0]
            val coords = Coordinates(
                wall = parts[1].toInt(),
                shelf = parts[2].toInt(),
                volume = parts[3].toInt(),
                page = parts[4].toInt()
            )
            val content = decryptPage(hex, coords, dict)
            val title = getTitle(coords, dict)

            return Result.success(
                    SearchResult(
                    coordinates = coords,
                    hex = hex,
                    title = title,
                    content = content,
                    address = address,
                    dictionaryId = dict.id
                )
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun generateRandomCoordinates(dict: Dictionary): Coordinates {
        return Coordinates(
            wall = (Math.random() * dict.walls + 1).toInt(),
            shelf = (Math.random() * dict.shelves + 1).toInt(),
            volume = (Math.random() * dict.volumes + 1).toInt(),
            page = (Math.random() * dict.pages + 1).toInt()
        )
    }

    private fun Coordinates.withPage() =
        if (page == 0) copy(page = 1) else this

    private fun getLocationHash(coords: Coordinates): Int {
        val hashInput = "${coords.wall}${coords.shelf}" +
                "${coords.volume.toString().padStart(2, '0')}" +
                "${coords.page.toString().padStart(3, '0')}"
        return sha512PrefixInt(hashInput)
    }

    private fun getTitle(
        coords: Coordinates,
        dict: Dictionary
    ): String {
        val locHash = getLocationHash(coords.copy(page = 0))
        val randomTitle = buildString {
            val rng = LcgRng(locHash)
            repeat(dict.lengthOfTitle) {
                append(dict.alphabet[rng.next(0, dict.alphabet.length)])
            }
        }
        return randomTitle
    }

    private fun decryptPage(
        hex: String,
        coords: Coordinates,
        dict: Dictionary
    ): String {
        val locHash = getLocationHash(coords)
        var result = transform(hex, locHash, encrypt = false)

        val fillHash = sha512PrefixInt(result)
        val rng = LcgRng(fillHash)
        while (result.length < dict.lengthOfPage) {
            result += dict.alphabet[rng.next(0, dict.alphabet.length)]
        }
        return formatContent(result.takeLast(dict.lengthOfPage))
    }

    private fun decryptTitle(
        hex: String,
        coords: Coordinates,
        dict: Dictionary
    ): String {
        val locHash = getLocationHash(coords.copy(page = 0))
        var result = transform(hex, locHash, encrypt = false)

        val fillHash = sha512PrefixInt(result)
        val rng = LcgRng(fillHash)
        while (result.length < dict.lengthOfTitle) {
            result += dict.alphabet[rng.next(0, dict.alphabet.length)]
        }
        return  result.takeLast(dict.lengthOfTitle)
    }

    private fun formatContent(text: String): String {
        return text.chunked(80).joinToString("\n")
    }

    private fun transform(
        input: String,
        seed: Int,
        encrypt: Boolean,
        dict: Dictionary = Dictionary.RUSSIAN
    )
    : String {
        val rng = LcgRng(seed)
        val (from, to) = if (encrypt)
            dict.alphabet to dict.digs
        else
            dict.digs to dict.alphabet

        return buildString {
            for (char in input) {
                val idx = from.indexOf(char)
                val rand = rng.next(0, to.length)
                val newIdx = mod(idx + if (encrypt) rand else -rand , to.length)
                append(to[newIdx])
            }
        }
    }

    private class LcgRng(seed: Int) {
        private var state: Long = seed.toLong() and 0xFFFFFFFFL
        fun next(min: Int = 0, max: Int): Int {
            state = (state * 22695477 + 1) and 0xFFFFFFFFL
            return min + (state / 4294967296.0 * (max - min)).toInt()
        }
    }

    private fun sha512PrefixInt(input: String): Int {
        val digest = MessageDigest.getInstance("SHA-512")
        val bytes = digest.digest(input.toByteArray())
        return bytes.take(4).fold(0) { acc, b ->
            (acc shl 8) or (b.toInt() and 0xFF)
        } and 0x0FFFFFFF
    }

    private fun mod(a: Int, b: Int) = ((a % b) + b) % b

    private fun generateRandomAddress(
        dict: Dictionary
    ): String {
        val coords = generateRandomCoordinates(dict)
        val randomHex = buildString {
            repeat(100) {append(dict.digs.random())}
        }
        return coords.toAddress(randomHex)
    }

    private fun generateRandomHex(
        length: Int = 100,
        dict: Dictionary): String {
        return buildString {
            repeat(length) {append(dict.digs.random())}
        }
    }

}