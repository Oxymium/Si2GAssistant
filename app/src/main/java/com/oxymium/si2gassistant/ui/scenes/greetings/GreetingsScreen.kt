package com.oxymium.si2gassistant.ui.scenes.greetings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oxymium.si2gassistant.R
import com.oxymium.si2gassistant.domain.entities.User
import com.oxymium.si2gassistant.domain.states.AppState
import com.oxymium.si2gassistant.domain.states.GreetingsState
import com.oxymium.si2gassistant.ui.AppEvent
import com.oxymium.si2gassistant.ui.scenes.animations.GreetingsAnimation
import com.oxymium.si2gassistant.ui.scenes.animations.LoadingAnimation
import com.oxymium.si2gassistant.ui.scenes.greetings.components.AnnouncementFeed
import com.oxymium.si2gassistant.ui.theme.MenuAccent
import com.oxymium.si2gassistant.ui.theme.Neutral
import com.oxymium.si2gassistant.ui.theme.Si2GAssistantTheme
import com.oxymium.si2gassistant.ui.theme.White
import com.oxymium.si2gassistant.utils.DateUtils
import java.util.Calendar

@Composable
fun GreetingsScreen(
    state: GreetingsState,
    appState: AppState,
    appEvent: (AppEvent) -> Unit,
    event: (GreetingsEvent) -> Unit
) {

    if (state.isGreetingsMode) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Neutral
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                // LOGOUT BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                ) {

                    Button(
                        modifier = Modifier
                            .align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MenuAccent
                        ),
                        onClick = { appEvent.invoke(AppEvent.OnLogoutButtonClick) }
                    ) {

                        Icon(
                            modifier = Modifier
                                .background(MenuAccent)
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.ic_login_variant),
                            contentDescription = "Logout button",
                            tint = White
                        )
                    }

                }

                // HIDDEN FOR DEBUG PURPOSES
                // TESTING BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {

                    Button(
                        modifier = Modifier
                            .align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Neutral
                        ),
                        onClick = { event.invoke(GreetingsEvent.OnTestingButtonClick) }
                    ) {

                        Icon(
                            modifier = Modifier
                                .background(Neutral)
                                .size(24.dp),
                            painter = painterResource(id = R.drawable.ic_login_variant),
                            contentDescription = "Logout button",
                            tint = Neutral
                        )
                    }

                }

                // GREETINGS ANIMATION
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    GreetingsAnimation()
                }


                // GREETINGS TEXT
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .wrapContentSize(Alignment.Center)
                        .align(Alignment.Center)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    ) {

                        val todayInMillis =
                            DateUtils.convertMillisToDate(Calendar.getInstance().timeInMillis)

                        Text(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.CenterHorizontally),
                            text = todayInMillis,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "Welcome, ${appState.currentUser?.firstname} ${appState.currentUser?.lastname}",
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "${appState.currentUser?.mail}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "${appState.currentUser?.academy}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                    }

                }

                // ANNOUNCEMENT TAPE
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        // ANNOUNCEMENT FEED
                        if (state.isAnnouncementsLoading) {

                            LoadingAnimation(
                                modifier = Modifier
                                    .fillMaxWidth()
                            )

                        } else {

                            Text(
                                modifier = Modifier
                                    .padding(
                                        horizontal = 8.dp
                                    ),
                                text = "Announcement feed",
                                color = White
                            )

                            AnnouncementFeed(
                                state = state
                            )

                        }

                    }

                }

            }

        }

    }

    if (state.isTestingMode) {
        TestingScreen(
            event = event
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingsScreenPreview() {
    Si2GAssistantTheme {
        val greetingsState = GreetingsState()
        val appStatePreview = AppState(
            currentUser = User(
                "",
                "gmail@test.net",
                "Grenoble",
                "Jon",
                "Doe",
                false
            )
        )
        GreetingsScreen(
            state = greetingsState,
            appState = appStatePreview,
            appEvent = {},
            event =  {}
        )
    }
}