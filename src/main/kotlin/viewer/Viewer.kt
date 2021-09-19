package viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import content.Collection
import content.ImageMedia
import content.Media

enum class ViewMode {
    /**
     * メディア単一表示
     */
    Single,

    /**
     * メディア一斉表示
     */
    Scroll,

    /**
     * コレクション一覧表示
     */
    Collection,
}

/**
 * メディアを一括表示するビューア
 */
@Composable
fun ScrollMediaViewer(
    modifier: Modifier = Modifier.fillMaxSize(),
    contents: List<Media>,
    target: Int,
    onViewerChange: (ViewMode, Int) -> Unit,
) {
    val scrollState = LazyListState(target, 0)

    LazyRow(
        modifier,
        state = scrollState,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        itemsIndexed(
            contents,
            key = { _, item -> item.path }
        ) { idx, item ->
            Box(Modifier.padding(16.dp).clickable {
                onViewerChange(ViewMode.Single, idx)
            }) {
                item.view()
            }
        }
    }
}

/**
 * コレクションを一覧表示するビューア
 */
@Composable
fun ScrollCollectionViewer(
    modifier: Modifier = Modifier.fillMaxSize(),
    contents: List<Collection>,
    onClickCollection: (Collection) -> Unit,
    onViewerChange: (ViewMode) -> Unit
) {
    Box(modifier) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            items(contents) { collection ->
                Box(Modifier.padding(8.dp)
                    .clickable {
                        onClickCollection(collection)
                    }
                ) {
                    collection.view()
                }
            }
        }
        // FABを表示
        FloatingActionButton(
            onClick = {
                onViewerChange(ViewMode.Scroll)
            },
            Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Default.ViewArray, contentDescription = "一覧表示に戻る")
        }
    }
}