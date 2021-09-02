import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import content.Collection
import java.nio.file.Path
import javax.swing.JFileChooser

@Composable
fun App(composeWindow: ComposeWindow) {
    var collection: Collection by remember {
        mutableStateOf(kotlin.run {
            var dirPath: Path?
            val dialog = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "Open"
            }

            do {
                // ダイアログを表示
                dialog.showOpenDialog(composeWindow)
                dirPath = dialog.selectedFile?.toPath()
            } while (dirPath == null)

            Collection(dirPath)
        })
    }

    ViewerContainer(collection).view { collection = it }
}