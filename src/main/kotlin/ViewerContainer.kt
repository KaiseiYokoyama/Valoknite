import androidx.compose.foundation.clickable
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
    collection: Collection,
    changeCollectionTo: (Collection) -> Unit
) {
    var mediaSet = collection.mediaList.toSet()
    var orderBy by remember { mutableStateOf(OrderBy(OrderBy.Order.Descending, OrderBy.By.Date)) }
    var viewMode by remember { mutableStateOf(ViewMode.Scroll) }
    val contents = Contents(mediaSet, orderBy)
    var target by remember { mutableStateOf(contents.mediaList()[0]) }

    val onViewModeChange = { newMode: ViewMode, media: Media ->
        viewMode = newMode
        target = media
    }

    if (viewMode == ViewMode.Single) {
        SingleMediaViewer(
            contents = contents, target = target, onViewerChange = onViewModeChange
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
                    Text(collection.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(collection.path.toString(), fontWeight = FontWeight.Light, fontSize = 8.sp)
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
                    items(collection.subCollections) { collection ->
                        Card(
                            Modifier.padding(10.dp)
                                .clickable {
                                    changeCollectionTo(collection)
                                },
                            elevation = 4.dp
                        ) {
                            collection.viewAsThumbnail()
                        }
                    }
                }
            }

            if (viewMode == ViewMode.Scroll) {
                ScrollMediaViewer(
                    contents = contents,
                    target = target,
                    onViewerChange = onViewModeChange
                )
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