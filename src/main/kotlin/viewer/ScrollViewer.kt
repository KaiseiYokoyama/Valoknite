package viewer

import OrderBy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import content.Collection
import content.Media
import kotlinx.coroutines.*

/**
 * メディアを一括表示するビューア
 */
class ScrollViewer(collection: Collection, orderBy: OrderBy) : Viewer(collection, orderBy) {
    private val scrollState = LazyListState(0, 0)
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun show(media: Media): Boolean {
        if (!mediaList.contains(media)) {
            return false
        }

        val idx = mediaList.indexOf(media)
        coroutineScope.launch {
            scrollState.scrollToItem(idx)
        }
        return true
    }

    override fun orderBy(newOrderBy: OrderBy) {
        super.orderBy(newOrderBy)
        // TODO: (可能なら)元の位置にスクロールしたい
    }

    @Composable
    override fun view(onViewerChange: (ViewMode, Media) -> Unit, orderBy: OrderBy) {
        val scrollState = rememberSaveable(saver = LazyListState.Saver) { scrollState }
        orderBy(orderBy)

        LazyRow(
            Modifier.fillMaxSize(),
            state = scrollState,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            items(mediaList) { item: Media ->
                Box(Modifier.padding(16.dp).clickable {
                    onViewerChange(ViewMode.Single, item)
                }) {
                    item.view()
                }
            }
        }
    }

}