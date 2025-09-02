package ru.lavafrai.maiapp.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import maiapp.composeapp.generated.resources.Res
import maiapp.composeapp.generated.resources.RobotoFlex
import org.jetbrains.compose.resources.Font

@Composable
fun rememberTypography(): Typography {
    val res = Res.font.RobotoFlex

    val fontFamily = FontFamily(
        Font(Res.font.RobotoFlex, FontWeight.W100),
        Font(Res.font.RobotoFlex, FontWeight.W200),
        Font(Res.font.RobotoFlex, FontWeight.W300),
        Font(Res.font.RobotoFlex, FontWeight.W400),
        Font(Res.font.RobotoFlex, FontWeight.W500),
        Font(Res.font.RobotoFlex, FontWeight.W600),
        Font(Res.font.RobotoFlex, FontWeight.W700),
        Font(Res.font.RobotoFlex, FontWeight.W800),
        Font(Res.font.RobotoFlex, FontWeight.W900),
    )

    val defaultTypography = Typography()
    return remember {
        Typography(
            displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
            labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
            labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily),
        )
    }
}
