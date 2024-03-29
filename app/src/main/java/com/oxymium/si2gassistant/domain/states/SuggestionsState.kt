package com.oxymium.si2gassistant.domain.states

import com.oxymium.si2gassistant.domain.entities.Suggestion

data class SuggestionsState(
    val suggestions: List<Suggestion> = emptyList(),
    val isSuggestionsLoading: Boolean = false,
    val isSuggestionsFailure: Boolean = false,
    val suggestionsFailureMessage: String? = null
)