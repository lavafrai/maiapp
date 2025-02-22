package ru.lavafrai.maiapp.fragments.account

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ru.lavafrai.maiapp.utils.toString

@Composable
fun MarkView(mark: Double) = MarkView(mark.toString(2))

@Composable
fun MarkView(mark: Int) = MarkView(mark.toString())

@Composable
fun MarkView(mark: String) {
    Text(text = mark)
}