@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.rootPages.login

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.HelpCircle
import kotlinx.coroutines.launch
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.BuildConfig.VERSION_NAME
import ru.lavafrai.maiapp.fragments.LoginPageButton
import ru.lavafrai.maiapp.fragments.fixes.SplitButtonLayoutFixed
import ru.lavafrai.maiapp.fragments.loginPageButtonColors
import ru.lavafrai.maiapp.platform.getPlatform
import ru.lavafrai.maiapp.theme.MaiColor

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun GreetingPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onLoginAsStudent: () -> Unit,
    onLoginAsTeacher: () -> Unit,
    onLoginLocalMode: () -> Unit,
) {
    Scaffold(
        containerColor = MaiColor,
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
        ) {
            val windowSizeClass = calculateWindowSizeClass()
            val columnFraction = when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 1f
                else -> 0.5f
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(columnFraction)
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(Res.drawable.mai_logo_exler),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.5f),
                )
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(Res.string.sign_in_as),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoginPageButton(
                    onClick = onLoginAsStudent,
                    text = stringResource(Res.string.student)
                )
                LoginPageButton(
                    onClick = onLoginAsTeacher,
                    text = stringResource(Res.string.teacher)
                )
                SplitButtonLayoutFixed(
                    modifier = Modifier.fillMaxWidth(),
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(onClick = onLoginLocalMode, colors = loginPageButtonColors(), modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.local_mode), fontSize = 20.sp)
                        }
                    },
                    trailingButton = {
                        val tooltipState = rememberTooltipState(isPersistent = true)
                        val scope = rememberCoroutineScope()
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(2.dp),
                            tooltip = { PlainTooltip(contentColor = Color.White.copy(alpha = .7f), containerColor = Color(0xFF141314)) { Text(stringResource(Res.string.local_mode_decription)) } },
                            state = tooltipState,
                        ) {
                            SplitButtonDefaults.TrailingButton(onClick = { scope.launch { tooltipState.show(
                                MutatePriority.UserInput
                            ) }}, colors = loginPageButtonColors()) {
                                Icon(FeatherIcons.HelpCircle, contentDescription = "Help about local mode")
                            }
                        }
                    },
                )
            }

            Box {
                Text(
                    "MAI app by. lava_frai\nBuild: $VERSION_NAME@${getPlatform().name()}",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                    color = Color.White.copy(alpha = .4f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}