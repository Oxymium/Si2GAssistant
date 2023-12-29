package com.oxymium.si2gassistant.domain.repository

import com.oxymium.si2gassistant.domain.entities.Suggestion
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface SuggestionRepository {

    fun getAllSuggestions(): Flow<List<Suggestion>>
    suspend fun submitSuggestion(suggestion: Suggestion): Deferred<String?>

}