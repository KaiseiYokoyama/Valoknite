import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.Path
import javax.swing.JFileChooser
import content.Collection
import content.Content
import content.Media
import viewer.ScrollViewer
import viewer.SingleViewer
import viewer.ViewMode
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.properties.Delegates

/**
 * メディアビューアのコンテナの振る舞いを定義するクラス。
 * @constructor コレクションを直接代入してインスタンス化する
 */
class ViewerContainer(collection: Collection) {
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
    var focusOn: Collection by Delegates.observable(collection) { _, _, _ ->
        singleViewer = SingleViewer(collection, orderBy)
        scrollViewer = ScrollViewer(collection, orderBy)
    }

    /**
     * メディアの並び順
     */
    var orderBy: OrderBy = OrderBy(OrderBy.Order.Descending, OrderBy.By.Date)

    /**
     * メディアを一つずつ表示するビューア
     * `focusOn`に変更があった時更新される
     */
    var singleViewer: SingleViewer = SingleViewer(collection, orderBy)

    /**
     * メディアを一括表示するビューア
     * `focusOn`に変更があった時更新される
     */
    var scrollViewer: ScrollViewer = ScrollViewer(collection, orderBy)

    /**
     * ビューモード
     */
    var viewMode = ViewMode.Scroll

    /**
     * コレクションの表示履歴
     */
    val history: ArrayList<Collection> = arrayListOf(collection)

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun view() {
        var viewMode by remember { mutableStateOf(viewMode) }
        var orderBy by remember { mutableStateOf(orderBy) }

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
                        Text(focusOn.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(focusOn.path.toString(), fontWeight = FontWeight.Light, fontSize = 8.sp)
                    }
                },
                actions = {
                    if (viewMode == ViewMode.Scroll) {
                        orderBy.view { orderBy = it }
                    }
                }
            )

            Row {
                // ナビゲーションバー
                val navWidth = 100.dp
                Column(Modifier.width(navWidth)) {
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

                val onViewModeChange = { newMode: ViewMode, item: Media ->
                    val viewer = when (newMode) {
                        ViewMode.Single -> singleViewer
                        ViewMode.Scroll -> scrollViewer
                    }
                    if (viewer.show(item)) {
                        viewMode = newMode
                    }
                }
                when (viewMode) {
                    ViewMode.Scroll -> scrollViewer.view(onViewModeChange, orderBy)
                    ViewMode.Single -> singleViewer.view(onViewModeChange, orderBy)
                }
            }
        }
    }
}

data class OrderBy(var order: Order, val by: By) {
    enum class Order {
        Ascending {
            @Composable
            override fun icon() {
                Icon(Icons.Default.ArrowDropUp, "昇順")
            }
        },
        Descending {
            @Composable
            override fun icon() {
                Icon(Icons.Default.ArrowDropDown, "降順")
            }
        };

        @Composable
        abstract fun icon()
    }

    enum class By {
        Name, Size, Date;

        @Composable
        fun viewButton(state: OrderBy, onClick: (OrderBy) -> Unit) {
            val selected = state.by == this
            val by = this

            Button(
                onClick = {
                    val newState = if (selected) {
                        state.flipped()
                    } else {
                        OrderBy(Order.Descending, this)
                    }
                    onClick(newState)
                },
                elevation = null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(by.name)
                    if (selected) {
                        state.order.icon()
                    }
                }
            }
        }
    }

    val sorter: Comparator<Content>
        get() {
            val selector: (Content) -> Comparable<*> = when (by) {
                By.Name -> { it -> it.name }
                By.Size -> { it -> it.path.fileSize() }
                By.Date -> { it -> it.path.getLastModifiedTime() }
            }

            return when (order) {
                Order.Ascending -> compareBy(selector)
                Order.Descending -> compareByDescending(selector)
            }
        }

    fun flipped(): OrderBy {
        val order = when (order) {
            Order.Ascending -> Order.Descending
            Order.Descending -> Order.Ascending
        }

        return OrderBy(order, by)
    }

    @Composable
    fun view(onClick: (OrderBy) -> Unit) {
        val state = this
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            By.values().forEach { by ->
                by.viewButton(state, onClick)
            }
        }
    }
}