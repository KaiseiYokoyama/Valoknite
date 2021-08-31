package viewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import content.Collection
import content.Media

/**
 * メディアを一つずつ表示するビューア
 */
class SingleViewer(collection: Collection) : Viewer(collection) {
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

    @Composable
    override fun view(onViewerChange: (ViewMode, Media) -> Unit) {
        var content by remember { mutableStateOf(now()) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        Box(
            Modifier.onSizeChanged { size = it }
                .pointerMoveFilter(onMove = {
                    mousePosition = it
                    false
                }).clickable {
                    val width = size.width.dp
                    if (mousePosition.x.dp < width / 4) {
                        prev()?.let {
                            content = it
                        }
                    } else if (width * 3 / 4 < mousePosition.x.dp) {
                        next()?.let {
                            content = it
                        }
                    }
                }
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            content.view()
        }
    }

}