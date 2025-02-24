@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package ru.lavafrai.maiapp.fragments.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.AlertOctagon
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.data.Loadable
import ru.lavafrai.maiapp.data.settings.rememberSettings
import ru.lavafrai.maiapp.fragments.AnimatedIcon
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.LoadableView
import ru.lavafrai.maiapp.fragments.PageColumn
import ru.lavafrai.maiapp.localizers.localizeTypeControlName
import ru.lavafrai.maiapp.models.account.Mark
import ru.lavafrai.maiapp.models.account.Student
import ru.lavafrai.maiapp.models.account.StudentMarks
import ru.lavafrai.maiapp.utils.asDp
import ru.lavafrai.maiapp.utils.capitalizeWords
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
    modifier = Modifier.fillMaxSize(),
) {
    Spacer(Modifier.height(8.dp))
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

                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                            Text("${stringResource(Res.string.course)} ${selectedStudent?.course}")
                            Text("${stringResource(Res.string.group)} ${selectedStudent?.group}")
                            Text("${selectedStudent?.speciality} ${selectedStudent?.specialityCipher}")
                            Text("${selectedStudent?.educationLevel}")
                        }
                    }

                    Button(onClick = viewModel::signOut, modifier = Modifier.align(Alignment.Bottom)) {
                        Text(stringResource(Res.string.sign_out))
                    }
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
    marksLoadable: Loadable<StudentMarks>,
    retry: () -> Unit,
) = LoadableView(marksLoadable, retry = retry) { marks ->
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.statistics), style = MaterialTheme.typography.titleLarge)

            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                val academicDebtCount = remember(marks) { marks.debtCount() }
                val averageMark = remember(marks) { marks.averageMark() }

                Column {
                    Text("${stringResource(Res.string.recordbook)} ${marks.recordBook}")
                    //Text("${stringResource(Res.string.student)} ${marks.student.lastname} ${marks.student.firstname} ${marks.student.middlename}")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${stringResource(Res.string.average_mark)} - ")
                        if (!averageMark.isNaN()) MarkView(averageMark)
                        else Text(" ${stringResource(Res.string.no_data).lowercase()}")
                    }
                    Text("${stringResource(Res.string.academic_debt_count)} - $academicDebtCount")
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val marksBySemesters = remember(marks.marks) { marks.marks.groupBy { it.semester } }
            val currentSemester = remember(marksBySemesters) { marksBySemesters.keys.maxOrNull() }

            marksBySemesters.keys.sorted().reversed().forEach { semester ->
                val marksInSemester = marksBySemesters[semester]!!

                SemesterMarksView(
                    semesterNumber = semester,
                    marks = marksInSemester,
                    isCurrent = currentSemester == semester,
                )
            }
        }
    }
}

@Composable
fun SemesterMarksView(
    semesterNumber: Int,
    marks: List<Mark>,
    isCurrent: Boolean,
) {
    var opened by remember { mutableStateOf(isCurrent) }

    AppCard(onClick = { if (!opened) opened = true }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${stringResource(Res.string.semester)} $semesterNumber", style = MaterialTheme.typography.titleLarge)
            IconButton(
                onClick = { opened = !opened },
                modifier = Modifier.size(MaterialTheme.typography.titleLarge.fontSize.asDp)
            ) {
                AnimatedIcon(
                    iconPainter = FeatherIcons.ChevronUp,
                    enabledIconPainter = FeatherIcons.ChevronDown,
                    enabled = opened,
                )
            }
        }

        AnimatedVisibility(visible = opened) {
            Column {
                Spacer(Modifier.height(16.dp))
                marks.forEach { mark ->
                    Box(
                        modifier = Modifier
                            .alpha(0.1f)
                            .background(LocalContentColor.current)
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                    MarkInfoView(mark)
                }
            }
        }
    }
}

@Composable
fun MarkInfoView(mark: Mark) = Column(
    modifier = Modifier
        .padding(vertical = 6.dp),
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = mark.name.trim())
            if (mark.lecturer.isNotBlank()) Text(
                text = mark.lecturer.replace(".", ". ").replace("  ", " ").capitalizeWords(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "${mark.hours} ${stringResource(Res.string.hours_genitive).lowercase()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        Spacer(Modifier.width(8.dp))
        MarkView(mark.value)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(onClick = { }, label = { Text(mark.typeControlName.localizeTypeControlName()) })
        if (mark.attempts > 1) AssistChip(
            onClick = { },
            label = { Text("${mark.attempts} ${stringResource(Res.string.attempts).lowercase()}") })
        if (mark.isDebt) AssistChip(onClick = { }, label = { Text(stringResource(Res.string.academic_debt)) })
    }
}
