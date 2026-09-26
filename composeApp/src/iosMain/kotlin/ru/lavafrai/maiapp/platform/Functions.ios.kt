package ru.lavafrai.maiapp.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PlatformImeOptions
import coil3.ImageLoader
import platform.UIKit.UITextContentTypePassword
import platform.UIKit.UITextContentTypeUsername

actual fun Modifier.pointerCursor(): Modifier = this

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalComposeUiApi::class)
actual fun credentialImeOptions(field: CredentialField): PlatformImeOptions? = PlatformImeOptions {
    // Native UIKit editing is what lets iOS offer saved passwords from the Keychain
    usingNativeTextInput(true)
    // Explicit, or the login field gets the email content type from KeyboardType.Email
    textContentType(
        when (field) {
            CredentialField.Username -> UITextContentTypeUsername
            CredentialField.Password -> UITextContentTypePassword
        }
    )
}

actual fun ImageLoader.Builder.trimMemoryCacheInBackground(): ImageLoader.Builder = this
