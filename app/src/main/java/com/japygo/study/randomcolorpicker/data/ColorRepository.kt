package com.japygo.study.randomcolorpicker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ColorRepository(private val context: Context) {
    private val RECENT_COLORS_KEY = stringPreferencesKey("recent_colors")
    private val SAVED_COLORS_KEY = stringPreferencesKey("saved_colors")

    val recentColorsFlow: Flow<List<Long>> = context.dataStore.data
        .map { preferences ->
            val colorsString = preferences[RECENT_COLORS_KEY] ?: ""
            if (colorsString.isEmpty()) {
                emptyList()
            } else {
                colorsString.split(",").mapNotNull { it.toLongOrNull() }
            }
        }

    val savedColorsFlow: Flow<List<Long>> = context.dataStore.data
        .map { preferences ->
            val colorsString = preferences[SAVED_COLORS_KEY] ?: ""
            if (colorsString.isEmpty()) {
                emptyList()
            } else {
                colorsString.split(",").mapNotNull { it.toLongOrNull() }
            }
        }

    suspend fun saveRecentColors(colors: List<Long>) {
        context.dataStore.edit { preferences ->
            preferences[RECENT_COLORS_KEY] = colors.joinToString(",")
        }
    }

    suspend fun addRecentColor(color: Long) {
        context.dataStore.edit { preferences ->
            val currentString = preferences[RECENT_COLORS_KEY] ?: ""
            val currentList = if (currentString.isEmpty()) {
                emptyList()
            } else {
                currentString.split(",").mapNotNull { it.toLongOrNull() }
            }
            
            // Add new color to top, distinct, take 5
            val newList = (listOf(color) + currentList).distinct().take(5)
            preferences[RECENT_COLORS_KEY] = newList.joinToString(",")
        }
    }

    suspend fun addSavedColor(color: Long) {
        context.dataStore.edit { preferences ->
            val currentString = preferences[SAVED_COLORS_KEY] ?: ""
            val currentList = if (currentString.isEmpty()) {
                emptyList()
            } else {
                currentString.split(",").mapNotNull { it.toLongOrNull() }
            }

            // Persistence logic: LIFO (Stack) max 5, like Recent Colors
            val newList = (listOf(color) + currentList).distinct().take(5)
            preferences[SAVED_COLORS_KEY] = newList.joinToString(",")
        }
    }

    suspend fun removeSavedColor(color: Long) {
        context.dataStore.edit { preferences ->
            val currentString = preferences[SAVED_COLORS_KEY] ?: ""
            if (currentString.isNotEmpty()) {
                val currentList = currentString.split(",").mapNotNull { it.toLongOrNull() }
                val newList = currentList.filter { it != color }
                preferences[SAVED_COLORS_KEY] = newList.joinToString(",")
            }
        }
    }
}
