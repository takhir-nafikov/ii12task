package org.ufku.ii12task

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ii12task",
    ) {
        App()
    }
}