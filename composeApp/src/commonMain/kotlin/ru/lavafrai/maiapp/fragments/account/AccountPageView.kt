package ru.lavafrai.maiapp.fragments.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.viewmodels.account.AccountViewModel
import ru.lavafrai.maiapp.viewmodels.account.AccountViewState
import androidx.compose.ui.Modifier
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.account_unsupported
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountPageView(
    viewModel: AccountViewModel,
    viewState: AccountViewState,
) = PageColumn (
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    paddings = true,
) {
    LoadableView(viewState.studentInfo, retry = viewModel::refresh) { studentInfo ->
        if (studentInfo == null) UnsupportedAccountView()
        else Column {
            AppCard {
                studentInfo.lastname?.let { Text("Lastname: $it") }
            }
        }
    }
}

@Composable
fun UnsupportedAccountView() {
    AppCard(
        modifier = Modifier.fillMaxWidth(0.8f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(FeatherIcons.AlertOctagon, contentDescription = "Unsupported account warning")
        Text(stringResource(Res.string.account_unsupported))
    }
}