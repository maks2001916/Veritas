package com.example.babylonianlibrary.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.apply

class DictionaryManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("library_dictionaries", Context.MODE_PRIVATE)

    private val gson = Gson()

    private val KEY_CUSTOM_DICTIONARIES = "custom_dictionaries"
    private val KEY_SELECTED_DICTIONARY_ID = "selected_dictionary_id"

    fun getAllDictionaries(): List<Dictionary> {
        val builtIn = Dictionary.getBuiltIn()
        val custom = getCustomDictionaries()
        return builtIn + custom
    }

    fun getCustomDictionaries(): List<Dictionary> {
        val json = prefs.getString(KEY_CUSTOM_DICTIONARIES, null) ?: return emptyList()
        val type = object : TypeToken<List<Dictionary>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDictionary(dictionary: Dictionary): Result<Dictionary> {
        val validation = dictionary.validate()
        if (!validation.isValid) {
            return Result.failure(Exception(validation.errors.joinToString("\n")))
        }
        val existing = getAllDictionaries().find { it.id == dictionary.id }
        if (existing != null && !existing.isDefault) {
            return Result.failure(Exception("Словарь с таким ID уже существует"))
        }

        val custom = getCustomDictionaries().toMutableList()
        custom.add(dictionary)
        val json = gson.toJson(custom)
        prefs.edit().putString(KEY_CUSTOM_DICTIONARIES, json).apply()

        return Result.success(dictionary)
    }

    fun deleteDictionary(dictionaryId: String): Boolean {
        val custom = getCustomDictionaries().toMutableList()
        val removed = custom.removeAll { it.id == dictionaryId}

        if (removed) {
            val json = gson.toJson(custom)
            prefs.edit().putString(KEY_CUSTOM_DICTIONARIES, json).apply()

            if (removed) {
                val json = gson.toJson(custom)
                prefs.edit().putString(KEY_CUSTOM_DICTIONARIES, json).apply()

                if (getSelectedDictionaryId() == dictionaryId) {
                    setSelectedDictionaryId(Dictionary.RUSSIAN.id)
                }
            }
        }
        return removed
    }

    fun getSelectedDictionary(): Dictionary {
        val selectedId = getSelectedDictionaryId()
        return  getAllDictionaries().find { it.id == selectedId } ?: Dictionary.RUSSIAN
    }

    fun setSelectedDictionary(dictionaryId: String) {
        val dictionary = getAllDictionaries().find { it.id == dictionaryId }
        if (dictionary != null) {
            setSelectedDictionaryId(dictionaryId)
        }
    }

    private fun getSelectedDictionaryId(): String {
        return prefs.getString(KEY_SELECTED_DICTIONARY_ID, Dictionary.RUSSIAN.id) ?: Dictionary.RUSSIAN.id

    }

    private fun setSelectedDictionaryId(id: String) {
        prefs.edit().putString(KEY_SELECTED_DICTIONARY_ID, id).apply()
    }

    fun exportDictionary(dictionaryId: String): String? {
        val dictionary = getAllDictionaries().find { it.id == dictionaryId }
        return dictionary?.let { gson.toJson(it) }
    }

    fun importDictionary(json: String): Result<Dictionary> {
        return try {
            val dictionary = gson.fromJson(json, Dictionary::class.java)
            saveDictionary(dictionary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun resetDefault() {
        setSelectedDictionaryId(Dictionary.RUSSIAN.id)
    }
}