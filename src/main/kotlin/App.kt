import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Photo
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import content.Collection
import content.CollectionException
import content.CollectionLoadException
import content.NoMediaCollectionException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App(composeWindow: ComposeWindow) {
    var rootCollection: Collection? by remember { mutableStateOf(null) }
    // ロード中か否か
    var loading: Boolean by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // CollectionLoadExceptionDialog
    var collectionLoadExceptionDialog: Boolean by remember { mutableStateOf(false) }
    if (collectionLoadExceptionDialog)
        AlertDialog(
            modifier = Modifier.width(400.dp),
            onDismissRequest = { collectionLoadExceptionDialog = false },
            title = {
                Row(horizontalArrangement = Arrangement.Start) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "フォルダをドロップしてください")
                    Text("フォルダをドロップしてください")
                }
            },
            text = { Text("コレクションとしてロードできるのはフォルダのみです") },
            buttons = {}
        )
    // NoMediaCollectionExceptionDialog
    var noMediaCollectionExceptionDialog: Boolean by remember { mutableStateOf(false) }
    if (noMediaCollectionExceptionDialog)
        AlertDialog(
            modifier = Modifier.width(400.dp),
            onDismissRequest = { noMediaCollectionExceptionDialog = false },
            title = {
                Row(horizontalArrangement = Arrangement.Start) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "メディア",
                            Modifier.graphicsLayer {
                                scaleX = 0.9f
                                scaleY = 0.9f
                            },
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "なし",
                            Modifier.graphicsLayer {
                                scaleX = 1.3f
                                scaleY = 1.3f
                            },
                            tint = MaterialTheme.colors.error
                        )
                    }
                    Text("メディアが入っていません")
                }
            },
            text = { Text("Valokniteが表示できるメディアがコレクションに入っていませんでした") },
            buttons = {}
        )

    if (rootCollection != null) {
        ViewerContainer(ViewerContainerState.new(rootCollection!!))
    } else if (loading) {
        LoadingScreen()
    } else {
        FileDroppableArea(composeWindow) {
            // ロードに移行したことをユーザに示す
            loading = true
            coroutineScope.launch {
                rootCollection = try {
                    Collection.new(it.toPath())
                } catch (e: CollectionLoadException) {
                    // フォルダでないものを読み込んだとき
                    collectionLoadExceptionDialog = true
                    null
                } catch (e: NoMediaCollectionException) {
                    // メディアの入ってないフォルダを読み込んだとき
                    noMediaCollectionExceptionDialog = true
                    null
                }
                // ロードを終えたことをユーザに示す
                loading = false
            }
        }
    }
}

/**
 * フォルダをドラッグ&ドロップして開くためのエリア
 */
@Composable
fun FileDroppableArea(composeWindow: ComposeWindow, onDropped: (File) -> Unit) {
    // ファイルをドロップしてください的なメッセージ
    Surface(contentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)) {
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
    Surface(contentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)) {
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