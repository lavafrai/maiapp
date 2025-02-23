@file:OptIn(ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.fragments.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.models.account.Student
import ru.lavafrai.maiapp.models.account.StudentMarks
import ru.lavafrai.maiapp.viewmodels.account.AccountViewModel
import ru.lavafrai.maiapp.viewmodels.account.AccountViewState

@Composable
fun AccountPageView(
    viewModel: AccountViewModel,
    viewState: AccountViewState,
) = PageColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    paddings = true,
) {
    val settings by rememberSettings()
    val selectedStudent = viewState.student.data

    LoadableView(viewState.studentInfo, retry = viewModel::refresh) { studentInfo ->
        AppCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (studentInfo.students.isEmpty()) {
                UnsupportedAccountView()
            } else {

                Text(stringResource(Res.string.student), style = MaterialTheme.typography.titleLarge)

                Text(
                    "${studentInfo.lastname} ${studentInfo.firstname} ${studentInfo.middlename}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (selectedStudent != null) StudentSelector(
                    students = studentInfo.students,
                    selected = selectedStudent,
                    onSelectedChange = { viewModel.setSelectedStudent(it) },
                )

                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                    Column {
                        Text("${stringResource(Res.string.course)} ${selectedStudent?.course}")
                        Text("${stringResource(Res.string.group)} ${selectedStudent?.group}")
                        Text("${selectedStudent?.speciality} ${selectedStudent?.specialityCipher}")
                        Text("${selectedStudent?.educationLevel}")
                    }
                }

                Button(onClick = viewModel::signOut, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(Res.string.sign_out))
                }
            }
        }
    }

    if (selectedStudent != null) MarksView(viewState.marks, retry = { viewModel.reloadMarks(selectedStudent) })
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

@Composable
fun StudentSelector(
    students: List<Student>,
    selected: Student,
    onSelectedChange: (Student) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = {},
    ) {
        OutlinedTextField(
            value = "Student code ${selected.studentCode}",
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = false,
            onDismissRequest = {},
        ) {
            students.forEach { student ->
                DropdownMenuItem(
                    text = { Text(text = "Student code ${student.studentCode}") },
                    onClick = { onSelectedChange(student) },
                )
            }
        }
    }
}

@Composable
fun MarksView(
    marks: Loadable<StudentMarks>,
    retry: () -> Unit,
) = LoadableView(marks, retry = retry) { marks ->
    AppCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.statistics), style = MaterialTheme.typography.titleLarge)

        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
            val academicDebtCount = remember(marks) { marks.debtCount() }
            val averageMark = remember(marks) { marks.averageMark() }

            Column {
                Text("${stringResource(Res.string.recordbook)} ${marks.recordBook}")
                //Text("${stringResource(Res.string.student)} ${marks.student.lastname} ${marks.student.firstname} ${marks.student.middlename}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stringResource(Res.string.average_mark)} ")
                    if (!averageMark.isNaN()) MarkView(averageMark)
                    else Text("- ${stringResource(Res.string.no_data).lowercase()}")
                }
                Text("${stringResource(Res.string.academic_debt_count)} - $academicDebtCount")
            }
        }
    }
}