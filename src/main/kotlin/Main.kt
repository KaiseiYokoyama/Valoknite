// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Valoknite",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        val count = remember { mutableStateOf(0) }
        val viewer = remember { mutableStateOf(Viewer(this.window)) }
        MaterialTheme {
//            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
//                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
//                    onClick = {
//                        count.value++
//                    }) {
//                    Text(if (count.value == 0) "Hello World" else "Clicked ${count.value}!")
//                }
//                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
//                    onClick = {
//                        count.value = 0
//                    }) {
//                    Text("Reset")
//                }
//            }
            viewer.component1().view()
        }
    }
}
