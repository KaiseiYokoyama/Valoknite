import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.Path
import javax.swing.JFileChooser
import content.Collection
import content.Content
import kotlin.properties.Delegates

/**
 * メディアビューアの振る舞いを定義するクラス。
 * @constructor コレクションを直接代入してインスタンス化する
 */
class Viewer(collection: Collection) {
    inner class ViewState(collection: Collection) {
        /**
         * 表示中のコレクションに含まれるコンテンツの一覧
         */
        val media: List<Content> = collection.subContents
            .toList()

        /**
         * 表示中のコンテンツのインデックス
         */
        private var index: Int = 0

        /**
         * `idx`版目のコンテンツ
         */
        fun get(idx: Int) = if (idx < 0 || media.size <= idx) {
            null
        } else {
            index = idx
            media[index]
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

    @Composable
    fun view() {
        val content = state.now()

        Column(
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                content.view()
            }

            // 同一コンテンツ内のメディアを一斉表示
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center,
//                modifier = Modifier.horizontalScroll(rememberScrollState())
//            ) {
//                state.media.forEach {
//                    Box(Modifier.padding(16.dp)) {
//                        it.view()
//                    }
//                }
//            }
        }
    }
}