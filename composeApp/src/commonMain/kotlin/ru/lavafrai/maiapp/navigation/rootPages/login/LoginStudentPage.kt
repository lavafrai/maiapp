@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.lavafrai.maiapp.navigation.rootPages.login

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.mai_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginStudentPage(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    with(sharedTransitionScope) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
            ) {
                Row {
                    Button(onClick = { navController.navigateUp() }) {
                        Text("back")
                    }
                    Text("test ")

                    Image(
                        painter = painterResource(Res.drawable.mai_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "logo"),
                                animatedVisibilityScope = animatedContentScope,
                            )
                            .fillMaxWidth(0.1f),
                    )
                }
            }
        }
    }
}