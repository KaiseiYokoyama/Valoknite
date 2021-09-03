import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import content.Collection
import content.Content
import content.Media
import viewer.*
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
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(state.collection.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(state.collection.path.toString(), fontWeight = FontWeight.Light, fontSize = 8.sp)
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

        Row {
            if (state.viewMode == ViewMode.Scroll) {
                Box {
                    ScrollMediaViewer(
                        contents = state.contents,
                        target = state.target,
                        onViewerChange = onViewModeChange
                    )
                    FloatingActionButton(
                        onClick = { state = state.viewMode(ViewMode.Collection) },
                        Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "コレクション一覧を表示")
                    }
                }
            } else if (state.viewMode == ViewMode.Collection) {
                Box {
                    ScrollCollectionViewer(
                        contents = state.collection.subCollections,
                        onClickCollection = {
                            state = ViewerContainerState.new(it).viewMode(ViewMode.Scroll)
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