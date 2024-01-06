package com.oxymium.si2gassistant.ui.scenes.submitsuggestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxymium.si2gassistant.currentUser
import com.oxymium.si2gassistant.domain.entities.Result
import com.oxymium.si2gassistant.domain.entities.Suggestion
import com.oxymium.si2gassistant.domain.repository.SuggestionRepository
import com.oxymium.si2gassistant.domain.states.SubmitSuggestionState
import com.oxymium.si2gassistant.domain.validators.SubmitSuggestionValidator
import com.oxymium.si2gassistant.loadingInMillis
import kotlinx.coroutines.delay
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
    private val suggestion = _suggestion.asStateFlow()

    private fun submitSuggestion(suggestion: Suggestion) {
        viewModelScope.launch {
            val suggestionFinalized = suggestion.copy(
                submittedAcademy = currentUser?.academy,
                submittedDate = Calendar.getInstance().timeInMillis,
                submittedBy = currentUser?.mail
            )
            suggestionRepository.submitSuggestion(suggestionFinalized).collect {
                when (it) {
                    // FAILURE
                    is Result.Failed -> _state.emit(
                        state.value.copy(
                            isSubmitSuggestionFailure = true,
                            isSubmitSuggestionFailureMessage = it.errorMessage
                        )
                    )
                    // LOADING
                    is Result.Loading -> { _state.emit(
                            state.value.copy(
                                isSubmitSuggestionLoading = true
                            )
                        )
                        delay(loadingInMillis)
                    }
                    // SUCCESS
                    is Result.Success -> _state.emit(
                        state.value.copy(
                            isSubmitSuggestionFailure = false,
                            isSubmitSuggestionFailureMessage = null,
                            isSubmitSuggestionLoading = false,
                        )
                    )
                }
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
                // Verify if any element is null
                val errors = listOfNotNull(
                    result.suggestionSubjectError,
                    result.suggestionBodyError
                )

                if (errors.isEmpty()) {
                    _state.value = state.value.copy(
                        isSubjectFieldError = false,
                        isBodyFieldError = false,
                    )

                    submitSuggestion(suggestion.value) // Submit new suggestion after validation

                } else {
                    if (!result.suggestionSubjectError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isSubjectFieldError = true
                        )
                    }
                    if (!result.suggestionBodyError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isBodyFieldError = true
                        )
                    }
                }

            }
        }
    }

}