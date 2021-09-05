import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import content.Collection
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File

@Composable
fun App(composeWindow: ComposeWindow) {
    val rootCollection: Collection? by remember { mutableStateOf(null) }
    val loading = mutableStateOf(false)

    if (rootCollection != null) {
        ViewerContainer(ViewerContainerState.new(rootCollection!!))
    } else if (loading.value) {
        LoadingScreen()
    } else {
        FileDroppableArea(composeWindow) {
            println(it.absolutePath)
        }
    }
}

/**
 * フォルダをドラッグ&ドロップして開くためのエリア
 */
@Composable
fun FileDroppableArea(composeWindow: ComposeWindow, onDropped: (File) -> Unit) {
    // ファイルをドロップしてください的なメッセージ
    Surface (contentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = "フォルダをドロップして開く",
                    Modifier.size(80.dp)
                )
                Text(
                    "コレクションとして開きたいフォルダを\nドラッグ&ドロップしてください",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    val target = object : DropTarget() {
        @Synchronized
        override fun drop(event: DropTargetDropEvent?) {
            if (event != null) {
                event.acceptDrop(DnDConstants.ACTION_REFERENCE)
                val droppedFiles = event.transferable
                    .getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                onDropped(droppedFiles[0] as File)
            }
        }
    }
    composeWindow.dropTarget = target
}

/**
 * ローディング中の画面
 */
@Composable
fun LoadingScreen() {
    Surface (contentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    Modifier.size(80.dp),
                )
                Text(
                    "ロード中",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}