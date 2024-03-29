package com.oxymium.si2gassistant.ui.scenes.submitsuggestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxymium.si2gassistant.currentUser
import com.oxymium.si2gassistant.domain.entities.Suggestion
import com.oxymium.si2gassistant.domain.repository.SuggestionRepository
import com.oxymium.si2gassistant.domain.states.SubmitSuggestionState
import com.oxymium.si2gassistant.domain.validators.SubmitSuggestionValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class SubmitSuggestionViewModel(
    private val suggestionRepository: SuggestionRepository
): ViewModel() {

    private val _state = MutableStateFlow(SubmitSuggestionState())
    val state = _state.asStateFlow()

    private val _suggestion = MutableStateFlow(Suggestion())
    val suggestion = _suggestion.asStateFlow()

    private fun submitSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            val suggestionFinalized = suggestion.copy(
                submittedAcademy = currentUser?.academy,
                submittedDate = Calendar.getInstance().timeInMillis,
                submittedBy = currentUser?.mail
            )

            // Push Suggestion
            try {
                suggestionRepository.submitSuggestion(suggestionFinalized)
                // SUCCESS
                _state.emit(
                    state.value.copy(
                        isSubmitSuggestionFailure = false,
                        isSubmitSuggestionFailureMessage = null
                    )
                )
            } catch (e: Exception) {
                // FAILURE
                _state.emit(
                    state.value.copy(
                        isSubmitSuggestionFailure = true,
                        isSubmitSuggestionFailureMessage = e.message
                    )
                )
            }

        }
    }

    fun onEvent(event: SubmitSuggestionEvent) {
        when (event) {

            is SubmitSuggestionEvent.OnSuggestionSubjectChange -> {
                val suggestion = suggestion.value.copy(
                    subject = event.suggestionSubject
                )
                _suggestion.value = suggestion
            }
            is SubmitSuggestionEvent.OnSuggestionBodyChange -> {
                val suggestion = suggestion.value.copy(
                    body = event.suggestionBody
                )
                _suggestion.value = suggestion
            }

            SubmitSuggestionEvent.OnSubmitSuggestionButtonClick -> {

                // Initial reset
                _state.value = state.value.copy(
                    isSubjectFieldError = false,
                    isBodyFieldError = false
                )

                val result = SubmitSuggestionValidator.validateSuggestion(suggestion.value)

                // Check if any errors
                if (result.hasErrors()) {
                    _state.value =
                        state.value.copy(
                            isSubjectFieldError = result.suggestionSubjectError,
                            isBodyFieldError = result.suggestionBodyError
                        )

                } else {
                    submitSuggestion(suggestion.value) // Submit new suggestion after validation
                }

            }
        }
    }

}