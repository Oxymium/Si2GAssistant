package com.oxymium.si2gassistant.ui.scenes.bugtickets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.oxymium.si2gassistant.domain.mock.provideRandomBugTicket
import com.oxymium.si2gassistant.domain.states.BugTicketsState
import com.oxymium.si2gassistant.ui.scenes.animations.LoadingAnimation
import com.oxymium.si2gassistant.ui.scenes.animations.NothingAnimation
import com.oxymium.si2gassistant.ui.scenes.bugtickets.components.BugTicketBottomSheet
import com.oxymium.si2gassistant.ui.scenes.bugtickets.components.BugTicketList
import com.oxymium.si2gassistant.ui.scenes.bugtickets.components.BugTicketSearch
import com.oxymium.si2gassistant.ui.theme.Si2GAssistantTheme


@Composable
fun BugTicketsScreen(
    state: BugTicketsState,
    event: (BugTicketsEvent) ->  Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            BugTicketSearch(
                state = state,
                event
            )

            if (state.isBugTicketsLoading) {

                LoadingAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                )

            } else {

                if (state.bugTickets.isEmpty()) {

                    NothingAnimation()

                } else {

                    BugTicketList(
                        state = state,
                        event
                    )

                }

            }

        }
    }

    BugTicketBottomSheet(
        state = state,
        event
    )

}

@Preview(showBackground = true)
@Composable
fun BugTicketsScreenPreview() {
    Si2GAssistantTheme {
        val previewState = BugTicketsState(
            bugTickets = List(20) { provideRandomBugTicket() },
            isBugTicketsLoading = false
        )
        BugTicketsScreen(
            state = previewState
        ) {

        }
    }
}