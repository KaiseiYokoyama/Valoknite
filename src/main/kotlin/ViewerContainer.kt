import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import content.Collection
import content.Content
import content.Media
import viewer.*
import java.util.Stack
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime

/**
 * メディアビューアのコンテナ
 */
@Composable
fun ViewerContainer(
    state: ViewerContainerState,
) {
    var state by remember { mutableStateOf(state) }

    val onViewModeChange = { newMode: ViewMode, media: Media ->
        state = state.viewMode(newMode).target(media)
    }

    if (state.viewMode == ViewMode.Single) {
        SingleMediaViewer(
            contents = state.contents, target = state.target, onViewerChange = onViewModeChange
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // TopAppBar
        TopAppBar(
            title = {
                Row(
                    Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    // nameとパス
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(state.collection.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(state.collection.path.toString(), fontWeight = FontWeight.Light, fontSize = 8.sp)
                    }
                    // 戻るボタン
                    if (!ViewerContainerState.history.empty()) {
                        Button(
                            onClick = {
                                val pop = ViewerContainerState.history.pop()
                                println("${pop.name}")
                                state = state.collection(pop, false)
                            },
                            elevation = null,
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                        }
                    }
                }
            },
            actions = {
                if (state.viewMode == ViewMode.Scroll) {
                    state.orderBy.view {
                        state = state.orderBy(it)
                    }
                }
            }
        )

        Box {
            Box {
                ScrollMediaViewer(
                    contents = state.contents,
                    target = state.target,
                    onViewerChange = onViewModeChange
                )
                if (state.collection.subCollections.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { state = state.viewMode(ViewMode.Collection) },
                        Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "コレクション一覧を表示")
                    }
                }
            }
            if (state.viewMode == ViewMode.Collection) {
                Box(
                    Modifier.clickable { state = state.viewMode(ViewMode.Scroll) }
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    ScrollCollectionViewer(
                        contents = state.collection.subCollections,
                        onClickCollection = {
                            state = state.collection(it).viewMode(ViewMode.Scroll)
                        }
                    )
                }
            }
        }
    }
}

/**
 * ViewerContainerの状態
 * インスタンスを作る時は`ViewerContainerState.new()`を推奨
 */
data class ViewerContainerState private constructor(
    val collection: Collection,
    val contents: List<Media>,
    val target: Media,
    val orderBy: OrderBy,
    val viewMode: ViewMode,
) {
    companion object {
        val history = Stack<Collection>()

        fun new(collection: Collection): ViewerContainerState {
            val orderBy = OrderBy(OrderBy.Order.Descending, OrderBy.By.Date)
            val mediaList = collection.mediaList.sortedWith(orderBy.sorter)

            return ViewerContainerState(
                collection,
                mediaList,
                mediaList[0],
                orderBy,
                ViewMode.Scroll
            )
        }
    }

    fun target(target: Media) = this.copy(target = target)
    fun orderBy(orderBy: OrderBy) = this.copy(contents = contents.sortedWith(orderBy.sorter), orderBy = orderBy)
    fun viewMode(viewMode: ViewMode) = this.copy(viewMode = viewMode)
    fun collection(collection: Collection, record: Boolean = true): ViewerContainerState {
        if (record) {
            history.push(this.collection)
        }
        return ViewerContainerState.new(collection).copy(orderBy = orderBy, viewMode = viewMode)
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