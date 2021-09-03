package viewer

import OrderBy
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import content.ImageMedia
import content.Media

enum class ViewMode {
    Single, Scroll,
}

data class Contents(
    val mediaSet: Set<Media>,
    val orderBy: OrderBy,
) {
    val size: Int
        get() = mediaSet.size

    fun mediaList(): List<Media> {
        println("Contents.mediaList")
        return mediaSet.sortedWith(orderBy.sorter)
    }
}

/**
 * メディアを一つずつ表示するビューア
 */
@Composable
fun SingleMediaViewer(
    modifier: Modifier = Modifier.fillMaxSize(),
    contents: Contents,
    target: Media,
    onViewerChange: (ViewMode, Media) -> Unit
) {
    val mediaList = contents.mediaList()
    var index by remember { mutableStateOf(mediaList.indexOf(target)) }

    // ズーム関係
    var size by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableStateOf(false) }

    // メディア切り替え関係
    var mousePosition = Offset.Zero

    MaterialTheme(
        colors = darkColors()
    ) {
        Surface {
            Box(
                modifier.onSizeChanged { size = it }
                    .pointerMoveFilter(onMove = {
                        mousePosition = it
                        false
                    })
                    .clickable {
                        val width = size.width.dp
                        if (mousePosition.x.dp < width / 4) {
                            val newIndex = kotlin.math.min(index + 1, contents.size)
                            if (index != newIndex) {
                                index = newIndex
                                zoom = false
                            }
                        } else if (width * 3 / 4 < mousePosition.x.dp) {
                            val newIndex = kotlin.math.max(index - 1, 0)
                            if (index != newIndex) {
                                index = newIndex
                                zoom = false
                            }
                        } else {
                            zoom = !zoom
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val media = mediaList[index]
                // メディアを一枚ずつ表示
                if (!zoom) {
                    media.view()
                } else {
                    if (media is ImageMedia) {
                        val horizontalScrollState by remember { mutableStateOf(ScrollState(0)) }
                        val verticalScrollState by remember { mutableStateOf(ScrollState(0)) }

                        val density = LocalDensity.current.run {
                            1.dp.toPx()
                        }
                        val wide = (size.width.toFloat() / size.height.toFloat()
                                > media.asset.width.toFloat() / media.asset.height.toFloat())
                        val modifier = if (wide) {
                            Modifier.width(size.width.dp / density)
                                .height(
                                    size.width.dp
                                            * (media.asset.height.toFloat() / media.asset.width.toFloat())
                                            / density
                                )
                        } else {
                            Modifier.height(size.height.dp / density)
                                .width(
                                    size.height.dp
                                            * (media.asset.width.toFloat() / media.asset.height.toFloat())
                                            / density
                                )
                        }
                        Box(
                            Modifier.horizontalScroll(horizontalScrollState)
                                .verticalScroll(verticalScrollState)
                                .fillMaxSize()
                        ) {
                            media.view(modifier)
                        }
                    } else {
                        media.view()
                    }
                }
                // FABを表示
                FloatingActionButton(
                    onClick = {
                        onViewerChange(ViewMode.Scroll, media)
                    },
                    Modifier.align(Alignment.BottomEnd).padding(16.dp),
                ) {
                    Icon(Icons.Default.ViewArray, contentDescription = "一覧表示に戻る")
                }
            }
        }
    }
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