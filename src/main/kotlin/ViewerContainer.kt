import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.Path
import javax.swing.JFileChooser
import content.Collection
import content.Content
import content.Media
import kotlin.properties.Delegates

/**
 * メディアビューアのコンテナの振る舞いを定義するクラス。
 * @constructor コレクションを直接代入してインスタンス化する
 */
class ViewerContainer(collection: Collection) {
    inner class ViewState(collection: Collection) {
        /**
         * 表示中のコレクションに含まれるコンテンツの一覧
         */
        val mediaList: List<Media> = collection.mediaList
            .toList()

        /**
         * 表示中のコンテンツのインデックス
         */
        private var index: Int = 0

        /**
         * `idx`番目のコンテンツ
         */
        fun get(idx: Int) = if (idx < 0 || mediaList.size <= idx) {
            null
        } else {
            index = idx
            mediaList[index]
        }

        /**
         * 次のコンテンツ
         */
        fun next(): Content? = get(index + 1)

        /**
         * 現在表示中のコンテンツ
         */
        fun now() = get(index)!!

        /**
         * 前のコンテンツ
         */
        fun prev(): Content? = get(index - 1)
    }

    /**
     * ファイルダイアログを表示して、ユーザにコレクションとして扱うフォルダを選んでもらう
     */
    constructor(composeWindow: ComposeWindow) : this(kotlin.run {
        var dirPath: Path?
        val dialog = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Open"
        }

        do {
            // ダイアログを表示
            dialog.showOpenDialog(composeWindow)
            dirPath = dialog.selectedFile?.let {
                it.toPath()
            }
        } while (dirPath == null)

        Collection(dirPath)
    })

    /**
     * 表示中のコレクション
     * 変更があった際にはstateも更新する
     */
    var focusOn: Collection by Delegates.observable(collection) { property, oldValue, newValue ->
        state = ViewState(newValue)
    }

    /**
     * 表示の状態
     * `focusOn`に変更があった時更新される
     */
    var state: ViewState = ViewState(collection)

    /**
     * コレクションの表示履歴
     */
    val history: ArrayList<Collection> = arrayListOf(collection)

    /**
     * マウスの位置
     */
    var mousePosition: Offset = Offset(0f, 0f)

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun view() {
        var content by remember { mutableStateOf(state.now()) }
        // viewer全体のサイズ
        var viewerSize by remember { mutableStateOf(IntSize.Zero) }

        Column(
            Modifier.onSizeChanged { viewerSize = it },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // TopAppBar
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(content.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(focusOn.path.toString(), fontWeight = FontWeight.Light, fontSize = 8.sp)
                    }
                }
            )

            // stateに従い画像を表示
//            Box(
//                Modifier.pointerMoveFilter(onMove = {
//                    mousePosition = it
//                    false
//                }).clickable {
//                    val width = size.width.dp
//                    if (mousePosition.x.dp < width / 4) {
//                        state.prev()?.let {
//                            content = it
//                        }
//                    } else if (width * 3 / 4 < mousePosition.x.dp) {
//                        state.next()?.let {
//                            content = it
//                        }
//                    }
//                }
//                    .fillMaxWidth()
//                    .fillMaxHeight(),
//                contentAlignment = Alignment.Center
//            ) {
//                content.view()
//            }

            Row {
                // ナビゲーションバー
                val navWidth = 100.dp
                Column (Modifier.width(navWidth)) {
                    // 戻るボタン
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        Modifier.fillMaxWidth().height(navWidth * 0.5f)
                    )
                    // 進むボタン
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        Modifier.fillMaxWidth().height(navWidth * 0.5f)
                    )
                    // コレクション一覧
                    LazyColumn(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        items(focusOn.subCollections) { collection ->
                            Card(Modifier.padding(10.dp), elevation = 4.dp) {
                                collection.viewAsThumbnail()
                            }
                        }
                    }
                }
                // 同一コンテンツ内のメディアを一斉表示
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    items(state.mediaList) { item: Media ->
                        Box(Modifier.padding(16.dp)) {
                            item.view()
                        }
                    }
                }
            }
        }
    }
}