package viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
class ScrollViewer(collection: Collection) : Viewer(collection) {
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

    @Composable
    override fun view() {
        val scrollState = rememberSaveable(saver = LazyListState.Saver) { scrollState }

        LazyRow(
            state = scrollState,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            items(mediaList) { item: Media ->
                Box(Modifier.padding(16.dp)) {
                    item.view()
                }
            }
        }
    }

}