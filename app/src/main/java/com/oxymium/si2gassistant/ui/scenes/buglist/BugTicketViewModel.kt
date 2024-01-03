package com.oxymium.si2gassistant.ui.scenes.buglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oxymium.si2gassistant.domain.entities.BugTicket
import com.oxymium.si2gassistant.domain.repository.BugTicketRepository
import com.oxymium.si2gassistant.domain.usecase.BugTicketFilter
import com.oxymium.si2gassistant.domain.usecase.BugTicketListEvent
import com.oxymium.si2gassistant.domain.usecase.BugTicketListState
import com.oxymium.si2gassistant.domain.entities.Result
import com.oxymium.si2gassistant.loadingInMillis
import com.oxymium.si2gassistant.ui.scenes.buglist.components.ResolvedCommentaryValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class BugTicketViewModel(private val bugTicketsRepository: BugTicketRepository): ViewModel() {

    private val _state = MutableStateFlow(BugTicketListState())
    private val _filter = MutableStateFlow<BugTicketFilter>(BugTicketFilter.DefaultValue)
    private val _selected = MutableStateFlow<BugTicket?>(null)

    val state = combine(
        _state, _filter, _selected
    ) { state, filter, selected ->
        state.copy(
            bugTickets = when (filter) {
                BugTicketFilter.DefaultValue -> state.bugTickets.sortedBy { it.submittedDate }.reversed() // sort by chronological order
                is BugTicketFilter.Search -> state.bugTickets.filter {
                    it.academy?.contains(filter.search ?: "", ignoreCase = true) == true ||
                            it.description?.contains(filter.search ?: "", ignoreCase = true) == true ||
                            it.shortDescription?.contains(filter.search ?: "", ignoreCase = true) == true
                }
            },
            selectedBugTicket = state.bugTickets.firstOrNull{ it.id == selected?.id },
            isSelectedBugTicketDetailsOpen = selected != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), BugTicketListState())

    init {
        getAllBugTickets()
    }

    private fun getAllBugTickets() {
        viewModelScope.launch {
            bugTicketsRepository.getAllBugTickets().collect {
                when (it) {
                    is Result.Failed -> _state.emit(
                        state.value.copy(
                            isBugTicketsFailed = true,
                            bugTicketsFailedMessage = it.errorMessage
                        )
                    )

                    is Result.Loading ->  { _state.emit(
                            state.value.copy(
                            isBugTicketsLoading = true
                        )
                    )
                        delay(loadingInMillis)

                    }

                    is Result.Success -> _state.emit(
                        state.value.copy(
                            isBugTicketsLoading = false,
                            isBugTicketsFailed = false,
                            bugTickets = it.data
                        ))
                }
            }
        }
    }

    private fun updateBugTicket(bugTicket: BugTicket) {
        viewModelScope.launch {
            val bugTicketUpdated = state.value.copy(
                selectedBugTicket = bugTicket.copy(
                    isResolved = true,
                    resolvedDate = Calendar.getInstance().timeInMillis,
                    resolvedComment = state.value.resolvedComment
                )
            )
            bugTicketUpdated.selectedBugTicket?.let {
                bugTicketsRepository.updateBugTicket(it).collect { result ->
                    when (result) {
                        // FAILURE
                        is Result.Failed -> _state.emit(
                            state.value.copy(
                                isUpdateBugTicketFailure = true,
                                isUpdateBugTicketFailureMessage = result.errorMessage
                            )
                        )

                        // LOADING
                        is Result.Loading -> { _state.emit(
                            state.value.copy(
                                isUpdateBugTicketLoading = true
                            )
                        )
                            delay(loadingInMillis)
                        }

                        // SUCCESS
                        is Result.Success -> _state.emit(
                            state.value.copy(
                                isUpdateBugTicketFailure = false,
                                isUpdateBugTicketFailureMessage = null,
                                isUpdateBugTicketLoading = false,
                            )
                        )
                    }
                }
            }
        }
    }

    private fun updateSelectedBugTicket(bugTicket: BugTicket?) {
        viewModelScope.launch {
            _selected.emit(bugTicket)
        }
    }

    private fun updateBugTicketFilter(bugTicketFilter: BugTicketFilter) {
        viewModelScope.launch {
            _filter.emit(bugTicketFilter)
        }
    }

    fun onEvent(event: BugTicketListEvent) {
        when(event) {
            is BugTicketListEvent.SelectBugTicket ->  updateSelectedBugTicket(event.bugTicket)
            BugTicketListEvent.DismissBugTicketDetailsSheet -> updateSelectedBugTicket(null)
            is BugTicketListEvent.OnResolvedCommentChanged -> _state.value = state.value.copy(resolvedComment = event.resolvedComment)
            is BugTicketListEvent.OnResolvedDetailsSheetButtonClick -> {

                // Initial reset
                _state.value = state.value.copy(
                    isResolvedCommentFieldError = false
                )

                // Get resolved comment
                val resolvedComment = state.value.resolvedComment ?: ""

                val result = ResolvedCommentaryValidator.validateResolvedCommentary(resolvedComment)

                // Verify if any element is null
                val errors = listOfNotNull(
                    result.resolvedCommentaryError
                )

                if (errors.isEmpty()) {
                    _state.value = state.value.copy(
                        isResolvedCommentFieldError = false
                    )

                    // Update bug ticket if conditions are met
                    state.value.copy().let {
                        state.value.selectedBugTicket?.let { bugTicket ->
                            updateBugTicket(
                                bugTicket
                            )
                        }
                    }

                } else {

                    if (!result.resolvedCommentaryError.isNullOrEmpty()) {
                        _state.value = state.value.copy(
                            isResolvedCommentFieldError = true
                        )
                    }

                }
            }
            is BugTicketListEvent.OnSearchTextInput -> updateBugTicketFilter(BugTicketFilter.Search(event.search))
        }
    }

}