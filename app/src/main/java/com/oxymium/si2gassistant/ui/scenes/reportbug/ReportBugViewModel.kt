package com.oxymium.si2gassistant.ui.scenes.reportbug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxymium.si2gassistant.currentUser
import com.oxymium.si2gassistant.domain.entities.BugTicket
import com.oxymium.si2gassistant.domain.entities.Result
import com.oxymium.si2gassistant.domain.repository.BugTicketRepository
import com.oxymium.si2gassistant.domain.states.ReportBugState
import com.oxymium.si2gassistant.domain.validators.ReportBugValidator
import com.oxymium.si2gassistant.loadingInMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportBugViewModel(
    private val bugTicketRepository: BugTicketRepository
): ViewModel() {

    private val _state = MutableStateFlow(ReportBugState())
    val state = _state.asStateFlow()

    private val _bugTicket = MutableStateFlow(BugTicket())
    private val bugTicket = _bugTicket.asStateFlow()

    init {
        getAllBugTicketsByUser()
    }
    private fun createBugTicket(bugTicket: BugTicket) {
        viewModelScope.launch {
            val bugTicketFinalized = bugTicket.copy(
                submittedDate = Calendar.getInstance().timeInMillis, // attach now to the submittedDate
                academy = currentUser?.academy,
                submittedBy = currentUser?.mail
            )
            bugTicketRepository.submitBugTicket(bugTicketFinalized).collect {
                when (it) {
                    // FAILURE
                    is Result.Failed -> _state.emit(
                        state.value.copy(
                            isSubmitBugTicketFailure = true,
                            submitBugTicketFailureMessage = it.errorMessage
                        )
                    )
                    // LOADING
                    is Result.Loading -> {
                        _state.emit(
                            state.value.copy(
                                isSubmitBugTicketLoading = true
                            )
                        )
                        delay(loadingInMillis)
                    }
                    // SUCCESS
                    is Result.Success -> _state.emit(
                        state.value.copy(
                            isSubmitBugTicketFailure = false,
                            submitBugTicketFailureMessage = null,
                            isSubmitBugTicketLoading = false
                        )
                    )

                }
            }
        }
    }

    private fun getAllBugTicketsByUser() {
        viewModelScope.launch {
            currentUser?.mail?.let {  mail ->
                bugTicketRepository.getBugTicketsByUser(mail).collect {
                    when (it) {
                        // FAILURE
                        is Result.Failed -> _state.emit(
                            state.value.copy(
                                isBugTicketsFailure = true,
                                bugTicketsFailureMessage = it.errorMessage
                            )
                        )
                        // LOADING
                        is Result.Loading -> {
                            _state.emit(
                                state.value.copy(
                                    isBugTicketsLoading = true
                                )
                            )
                            delay(loadingInMillis)
                        }
                        // SUCCESS
                        is Result.Success -> _state.emit(
                            state.value.copy(
                                isBugTicketsFailure = false,
                                bugTicketsFailureMessage = null,
                                isBugTicketsLoading = false,
                                bugTickets = it.data
                            )
                        )

                    }
                }
            }
        }
    }

    fun onEvent(event: ReportBugEvent) {
        when (event) {

            is ReportBugEvent.OnBugCategorySelect -> {
                val bugTicket = bugTicket.value.copy(
                    category = event.bugTicketCategory
                )
                _bugTicket.value = bugTicket
            }

            is ReportBugEvent.OnBugPrioritySelect -> {
                val bugTicket = bugTicket.value.copy(
                    priority = event.bugTicketPriority
                )
                _bugTicket.value = bugTicket
            }

            is ReportBugEvent.OnShortDescriptionChange -> {
                val bugTicket = bugTicket.value.copy(
                    shortDescription = event.shortDescription
                )
                _bugTicket.value = bugTicket
            }

            is ReportBugEvent.OnDescriptionChange -> {
                val bugTicket = bugTicket.value.copy(
                    description = event.description
                )
                _bugTicket.value = bugTicket
            }

            ReportBugEvent.OnBugTicketsModeButtonClick -> {
                val newState = state.value.copy(
                    bugTicketsMode = true,
                    submitBugTicketMode = false
                )
                _state.value = newState
            }
            ReportBugEvent.OnReportBugModeButtonClick -> {
                val newState = state.value.copy(
                    bugTicketsMode = false,
                    submitBugTicketMode = true
                )
                _state.value = newState
            }

            ReportBugEvent.OnReportBugButtonClick -> {

                val result = ReportBugValidator.validateBugTicket(bugTicket.value)

                // Verify if any element is null
                val errors = listOfNotNull(
                    result.bugTicketCategoryError,
                    result.bugTicketPriorityError,
                    result.bugTicketShortDescriptionError,
                    result.bugTicketDescriptionError
                )

                if (errors.isEmpty()) {
                    _state.value = state.value.copy(
                        isCategoryFieldError = false,
                        isPriorityFieldError = false,
                        isShortDescriptionFieldError = false,
                        isDescriptionFieldError = false,
                    )

                    createBugTicket(bugTicket.value) // Create new bug ticket after validation

                } else {

                    if (!result.bugTicketCategoryError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isCategoryFieldError = true
                        )
                    }

                    if (!result.bugTicketPriorityError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isPriorityFieldError = true
                        )
                    }

                    if (!result.bugTicketShortDescriptionError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isShortDescriptionFieldError = true
                        )
                    }

                    if (!result.bugTicketDescriptionError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isDescriptionFieldError = true
                        )
                    }

                }
            }

        }
    }
}