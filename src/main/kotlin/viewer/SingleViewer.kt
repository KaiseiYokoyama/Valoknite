package viewer

import OrderBy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import content.Collection
import content.ImageMedia
import content.Media

/**
 * メディアを一つずつ表示するビューア
 */
class SingleViewer(collection: Collection, orderBy: OrderBy) : Viewer(collection, orderBy) {
    /**
     * 表示中のメディアのインデックス
     */
    private var index: Int = 0

    /**
     * マウスカーソルの位置
     */
    var mousePosition: Offset = Offset(0f, 0f)

    /**
     * `idx`番目のメディアを取得
     */
    fun get(idx: Int) = if (idx < 0 || mediaList.size <= idx) {
        null
    } else {
        index = idx
        mediaList[index]
    }

    /**
     * 次のメディアを取得
     */
    fun next() = get(index + 1)

    /**
     * 現在表示中のメディアを取得
     */
    fun now() = get(index)!!

    /**
     * 前のメディアを取得
     */
    fun prev(): Media? = get(index - 1)

    override fun show(media: Media): Boolean {
        if (!mediaList.contains(media)) {
            return false
        }

        val idx = mediaList.indexOf(media)
        index = idx

        return true
    }

    override fun orderBy(newOrderBy: OrderBy) {
        val now = now()
        super.orderBy(newOrderBy)
        show(now)
    }

    @Composable
    override fun view(onViewerChange: (ViewMode, Media) -> Unit, orderBy: OrderBy) {
        var content by remember { mutableStateOf(now()) }
        var size by remember { mutableStateOf(IntSize.Zero) }
        orderBy(orderBy)

        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var mediaSize by remember { mutableStateOf(IntSize.Zero) }

        Box(
            Modifier.onSizeChanged { size = it }
                .pointerMoveFilter(onMove = {
                    mousePosition = it
                    false
                })
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        offset += pan
                    }
                }
                .clickable {
                    val width = size.width.dp
                    if (mousePosition.x.dp < width / 4) {
                        prev()?.let {
                            content = it
                            // reset transform
                            scale = 1f
                            offset = Offset.Zero
                        }
                    } else if (width * 3 / 4 < mousePosition.x.dp) {
                        next()?.let {
                            content = it
                            // reset transform
                            scale = 1f
                            offset = Offset.Zero
                        }
                    } else {
                        scale = if (scale != 1f) {
                            1f
                        } else {
                            kotlin.math.max(
                                size.width.toFloat() / mediaSize.width.toFloat(),
                                size.height.toFloat() / mediaSize.height.toFloat()
                            )
                        }
                    }
                }
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val content = content
            // メディアを一枚ずつ表示
            if (content is ImageMedia) {
                content.view(
                    Modifier.onSizeChanged { mediaSize = it }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        })
            } else {
                content.view()
            }
            // FABを表示
            FloatingActionButton(
                onClick = {
                    onViewerChange(ViewMode.Scroll, now())
                },
                Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                Icon(Icons.Default.ViewArray, contentDescription = "一覧表示に戻る")
            }
        }
    }

}