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
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import content.Media

enum class ViewMode {
    Single, Scroll,
}

data class Contents(
    val mediaSet: Set<Media>,
    val orderBy: OrderBy,
) {
    fun mediaList() = mediaSet.sortedWith(orderBy.sorter)
}

/**
 * メディアを一括表示するビューア
 */
@Composable
fun ScrollMediaViewer(
    modifier: Modifier = Modifier.fillMaxSize(),
    contents: Contents,
    target: Media,
    onViewerChange: (ViewMode, Media) -> Unit
) {
    val mediaList = contents.mediaList()
    val index = mediaList.indexOf(target)
    val scrollState by remember { mutableStateOf(LazyListState(index, 0)) }

    LazyRow(
        modifier,
        state = scrollState,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        items(mediaList) { item ->
            Box(Modifier.padding(16.dp).clickable {
                onViewerChange(ViewMode.Single, item)
            }) {
                item.view()
            }
        }
    }
}