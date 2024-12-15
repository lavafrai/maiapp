package ru.lavafrai.maiapp.fragments.schedule

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.lavafrai.maiapp.fragments.AppCard
import ru.lavafrai.maiapp.fragments.hypertext.Hypertext
import ru.lavafrai.maiapp.models.exler.ExlerTeacherReview

@Composable
fun TeacherReviewView(
    teacherReview: ExlerTeacherReview
) {
    val reviewHypertexts = (teacherReview.hypertext ?: "null").replace("\\n", "\n")

    AppCard {
        if (teacherReview.author != null) Row {
            Text("${stringResource(Res.string.author)}: ")
            Hypertext(teacherReview.author!!)
        }
        if (teacherReview.source != null) Row {
            Text("${stringResource(Res.string.source)}: ")
            Hypertext(teacherReview.source!!)
        }
        if (teacherReview.publishTime != null) Row {
            Text("${stringResource(Res.string.publishTime)}: ")
            Hypertext(teacherReview.publishTime!!)
        }
        Spacer(Modifier.height(8.dp))

        Hypertext(reviewHypertexts, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}