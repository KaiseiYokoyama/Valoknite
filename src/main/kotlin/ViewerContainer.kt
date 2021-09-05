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
import viewer.collection.MonoPreviewCollectionViewer
import java.util.Stack

/**
 * メディアビューアのコンテナ
 */
@Composable
fun ViewerContainer(
    state: ViewerContainerState,
) {
    var state by remember { mutableStateOf(state) }

    val onViewModeChange = { newMode: ViewMode, target: Int ->
        state = state.viewMode(newMode).target(target)
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
                        Text(state.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(state.path.toString(), fontWeight = FontWeight.Light, fontSize = 8.sp)
                    }
                    // 戻るボタン
                    if (!ViewerContainerState.history.empty()) {
                        Button(
                            onClick = {
                                val pop = ViewerContainerState.history.pop()
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
            if (state.contents.isNotEmpty()) {
                Box {
                    ScrollMediaViewer(
                        contents = state.contents,
                        target = state.target,
                        onViewerChange = onViewModeChange
                    )
                    if (state.subCollections.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { state = state.viewMode(ViewMode.Collection) },
                            Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = "コレクション一覧を表示")
                        }
                    }
                }
            }
            if (state.viewMode == ViewMode.Collection) {
                MonoPreviewCollectionViewer(
                    contents = state.subCollections,
                    onClickCollection = {
                        state = state.collection(it).viewMode(ViewMode.Scroll)
                    },
                    onViewerChange = { newMode -> state = state.viewMode(newMode) },
                    orderBy = state.orderBy,
                    onOrderChange = { newOrder -> state = state.orderBy(newOrder) },
                    displayFAB = state.contents.isNotEmpty()
                )
            }
        }
    }
}

/**
 * ViewerContainerの状態
 * インスタンスを作る時は`ViewerContainerState.new()`を推奨
 */
data class ViewerContainerState private constructor(
    private val collection: Collection,
    val contents: List<Media>,
    val subCollections: List<Collection>,
    val target: Int,
    val orderBy: OrderBy,
    val viewMode: ViewMode,
) {
    companion object {
        val history = Stack<Collection>()

        fun new(collection: Collection): ViewerContainerState {
            val orderBy = OrderBy.DEFAULT
            val mediaList = collection.mediaList.sortedWith(orderBy.sorter)
            val subCollections = collection.subCollections.sortedWith(orderBy.sorter)

            return ViewerContainerState(
                collection,
                mediaList,
                subCollections,
                0,
                orderBy,
                ViewMode.Scroll
            )
                // validate viewmode
                .viewMode(ViewMode.Scroll)
        }
    }

    val name = collection.name
    val path = collection.path

    fun target(target: Int) = this.copy(target = target)

    fun orderBy(orderBy: OrderBy) = this.copy(
        contents = contents.sortedWith(orderBy.sorter),
        subCollections = subCollections.sortedWith(orderBy.sorter),
        orderBy = orderBy
    )

    fun viewMode(viewMode: ViewMode): ViewerContainerState {
        val newMode = when (viewMode) {
            ViewMode.Scroll, ViewMode.Single -> {
                if (this.contents.isNotEmpty()) viewMode else ViewMode.Collection
            }
            ViewMode.Collection -> {
                if (this.subCollections.isNotEmpty()) viewMode else ViewMode.Scroll
            }
        }

        return this.copy(viewMode = newMode)
    }

    fun collection(collection: Collection, record: Boolean = true): ViewerContainerState {
        if (record) {
            history.push(this.collection)
        }
        return ViewerContainerState.new(collection).viewMode(viewMode).orderBy(orderBy)
    }
}

data class OrderBy(var order: Order, val by: By) {
    companion object {
        val DEFAULT: OrderBy
            get() = OrderBy(Order.Descending, By.Date)
    }

    enum class Order {
        Ascending {
            @Composable
            override fun icon(tint: Color?) {
                if (tint != null) {
                    Icon(Icons.Default.ArrowDropUp, "昇順", tint = tint)
                } else {
                    Icon(Icons.Default.ArrowDropUp, "昇順")
                }
            }
        },
        Descending {
            @Composable
            override fun icon(tint: Color?) {
                if (tint != null) {
                    Icon(Icons.Default.ArrowDropDown, "降順", tint = tint)
                } else {
                    Icon(Icons.Default.ArrowDropDown, "降順")
                }
            }
        };

        @Composable
        open fun icon(tint: Color? = null) {
        }
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
                elevation = null,
                contentPadding = PaddingValues(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(by.name)
                    if (selected) {
                        state.order.icon()
                    } else {
                        state.order.icon(Color.Black.copy(alpha = 0f))
                    }
                }
            }
        }
    }

    val sorter: Comparator<Content>
        get() {
            val selector: (Content) -> Comparable<*> = when (by) {
                By.Name -> { it -> it.name }
                By.Size -> { it -> it.size.value }
                By.Date -> { it -> it.lastMod }
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