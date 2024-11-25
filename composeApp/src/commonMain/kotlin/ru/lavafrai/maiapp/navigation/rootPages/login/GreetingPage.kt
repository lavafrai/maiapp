@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.login

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.BuildConfig.VERSION_NAME
import ru.lavafrai.maiapp.fragments.LoginPageButton
import ru.lavafrai.maiapp.navigation.Pages
import ru.lavafrai.maiapp.platform.getPlatformName
import ru.lavafrai.maiapp.theme.MaiColor
import ru.lavafrai.maiapp.viewmodels.login.LoginTarget
import ru.lavafrai.maiapp.viewmodels.login.LoginType

@Composable
fun GreetingPage(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
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
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(Res.drawable.mai_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.5f),
                )
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .verticalScroll(rememberScrollState()),
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
                    onClick = { navController.navigate(Pages.Login(LoginType.STUDENT, LoginTarget.ADD_SCHEDULE)) },
                    text = stringResource(Res.string.student)
                )
                LoginPageButton(
                    onClick = { navController.navigate(Pages.Login(LoginType.TEACHER, LoginTarget.ADD_SCHEDULE)) },
                    text = stringResource(Res.string.teacher)
                )
            }

            Box {
                Text(
                    "MAI app by. lava_frai\nBuild: $VERSION_NAME@${getPlatformName()}",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                    color = Color.White.copy(alpha = .4f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}