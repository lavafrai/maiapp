@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.navigation.rootPages.login

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.feathericons.X
import kotlinx.serialization.Serializable
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.start_typing
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.navigation.Pages
import ru.lavafrai.maiapp.viewmodels.LoginPageViewModel

@Composable
fun LoginPage(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    loginData: Pages.Login,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val viewModel: LoginPageViewModel = viewModel()
    val viewState by viewModel.state.collectAsState()

    Surface {
        val padding by animateDpAsState(if (isExpanded) 0.dp else 8.dp)

        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = {},
                            expanded = isExpanded,
                            onExpandedChange = { isExpanded = it },
                            leadingIcon = {
                                AnimatedVisibility(visible = !isExpanded, enter = fadeIn(), exit = fadeOut()) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
                                    }
                                }
                            },
                            trailingIcon = {
                                AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
                                    IconButton(onClick = { isExpanded = false }) {
                                        Icon(FeatherIcons.X, contentDescription = "Close")
                                    }
                                }
                            },
                            placeholder = { Text(stringResource(Res.string.start_typing)) },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                        .padding(top = padding),
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it },
                ) {

                }
            }
        }
    }
}

@Serializable
enum class LoginType {
    STUDENT,
    TEACHER,
}

@Serializable
enum class LoginTarget {
    ADD_SCHEDULE,
}