@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.rootPages.login

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import maiapp.composeapp.generated.resources.*
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.network_error
import maiapp.composeapp.generated.resources.start_typing
import maiapp.composeapp.generated.resources.wait_a_minute
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.BaseLoadableStatus
import ru.lavafrai.maiapp.fragments.ErrorView
import ru.lavafrai.maiapp.models.Nameable
import ru.lavafrai.maiapp.navigation.pages.LoginPage
import ru.lavafrai.maiapp.viewmodels.login.LoginPageViewModel
import soup.compose.material.motion.MaterialFadeThrough

@Composable
fun LoginPage(
    // navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onNavigateBack: () -> Unit,
    onLoginDone: (Nameable) -> Unit,
    loginData: LoginPage,
) {
    val focusRequester = remember { FocusRequester() }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val viewModel: LoginPageViewModel = viewModel(factory = LoginPageViewModel.Factory(loginData, onNavigateBack, onLoginDone))
    // val viewModel = viewModel { LoginPageViewModel(loginData, onNavigateBack, onLoginDone) }

    val viewState by viewModel.state.collectAsState()

    LaunchedEffect(viewState.neededLoadable.baseStatus) {
        if (viewState.neededLoadable.baseStatus == BaseLoadableStatus.Actual) {
            isExpanded = true
            focusRequester.requestFocus()
        } else {
            isExpanded = false
            focusRequester.freeFocus()
        }
    }

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
                            enabled = viewState.neededLoadable.hasData(),
                            query = searchQuery,
                            onQueryChange = { searchQuery = it ; viewModel.updateSearchQuery(it) },
                            onSearch = {},
                            expanded = isExpanded,
                            onExpandedChange = { isExpanded = it },
                            leadingIcon = {
                                AnimatedVisibility(visible = !isExpanded, enter = fadeIn(), exit = fadeOut()) {
                                    IconButton(onClick = { viewModel.onNavigateBack() }) {
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
                            placeholder = {
                                MaterialFadeThrough(
                                    targetState = viewState.neededLoadable.baseStatus,
                                ) { newScreen ->
                                    when (newScreen) {
                                        BaseLoadableStatus.Error -> Text(stringResource(Res.string.network_error))

                                        BaseLoadableStatus.Loading -> Text(stringResource(Res.string.wait_a_minute))

                                        BaseLoadableStatus.Actual -> Text(stringResource(Res.string.start_typing))
                                    }
                                }
                            },
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                        .padding(top = padding),
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it },
                ) {
                    LazyColumn {
                        items(viewState.filteredData) {
                            ListItem(
                                headlineContent = { Text(it.name) },
                                modifier = Modifier
                                    .animateItem()
                                    .clickable {
                                        viewModel.select(it)
                                        isExpanded = false
                                        searchQuery = it.name
                                    }
                            )
                        }
                        item {
                            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    // Empty space under search bar
                    MaterialFadeThrough(
                        targetState = viewState.neededLoadable.baseStatus,
                    ) { newScreen ->
                        when (newScreen) {
                            BaseLoadableStatus.Error -> ErrorView(
                                error = viewState.neededLoadable.error,
                                onRetry = { viewModel.startLoading() },
                            )

                            BaseLoadableStatus.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(Res.string.some_data_must_be_loaded))
                                Spacer(Modifier.height(8.dp))
                                CircularProgressIndicator()
                            }

                            BaseLoadableStatus.Actual -> {}//Text(stringResource(Res.string.choose_your_group_and_continue))
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    if (viewState.selected != null) Button(onClick = {
                        viewModel.login()
                    }) {
                        Text(stringResource(Res.string.go_next))
                        Spacer(Modifier.width(4.dp))
                        Icon(FeatherIcons.ArrowRight, "next")
                    }
                }
            }
        }
    }
}