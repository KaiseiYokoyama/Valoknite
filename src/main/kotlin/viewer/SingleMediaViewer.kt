package viewer

import MediaInspector
import Size
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import content.ImageMedia
import content.Media
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * メディアを一つずつ表示するビューア
 */
@OptIn(ExperimentalComposeUiApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun SingleMediaViewer(
    modifier: Modifier = Modifier.fillMaxSize(),
    contents: List<Media>,
    target: Int,
    onViewerChange: (ViewMode, Int) -> Unit
) {
    var index by remember { mutableStateOf(target) }

    // ズーム関係
    var zoom by remember { mutableStateOf(false) }

    // インスペクター
    var inspect by remember { mutableStateOf(false) }

    Background(
        Modifier,
        onFlipImage = {
            index = when (it) {
                FlipImageTo.Left -> max(index - 1, 0)
                FlipImageTo.Right -> min(index + 1, contents.size - 1)
            }
        },
        onChangeImageZoom = { zoom = it },
        onViewerChange = { onViewerChange(it, index) }
    ) {
        Row {
            if (inspect) {
                MediaInspector.view(Modifier.width(300.dp), contents[index])
            }
            var size by remember { mutableStateOf(IntSize.Zero) }
            CenteredBox(
                modifier.onSizeChanged { size = it }
                    .clickable { onViewerChange(ViewMode.Scroll, index) }
            ) {
                AnimatedMedia(index) {
                    // メディアを一枚ずつ表示
                    val media = contents[index]
                    ZoomableMedia(media, zoom, size) { zoom = it }
                }
                // FABを表示
                FloatingActionButton(
                    onClick = {
                        inspect = !inspect
                    },
                    Modifier.align(Alignment.BottomEnd).padding(16.dp),
                ) {
                    Icon(Icons.Default.ManageSearch, contentDescription = "インスペクターで詳細を表示")
                }
            }
        }
    }
}

private enum class FlipImageTo {
    Left, Right,
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Background(
    modifier: Modifier,
    // ユーザが左右の矢印キーを押して、前もしくは次の画像を見ようとした時
    onFlipImage: (flipImageTo: FlipImageTo) -> Unit,
    // ズームの有無を切り替えるとき
    onChangeImageZoom: (zoom: Boolean) -> Unit,
    // ユーザがビューアを変更しようとしたとき
    onViewerChange: (ViewMode) -> Unit,
    content: @Composable () -> Unit,
) {
    val reqr = FocusRequester()
    LaunchedEffect(Unit) { reqr.requestFocus() }

    Surface(
        modifier.onKeyEvent {
            if (it.type != KeyEventType.KeyDown) {
                false
            } else {
                when (it.key) {
                    Key.Escape -> {
                        onViewerChange(ViewMode.Scroll)
                        true
                    }
                    Key.DirectionLeft -> {
                        onFlipImage(FlipImageTo.Left)
                        onChangeImageZoom(false)
                        true
                    }
                    Key.DirectionRight -> {
                        onFlipImage(FlipImageTo.Right)
                        onChangeImageZoom(false)
                        true
                    }
                    else -> false
                }
            }
        }
            .focusRequester(reqr)
            .focusable()
    ) {
        content()
    }
}

@Composable
private fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) = Box(modifier, contentAlignment = Alignment.Center, content = content)

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedMedia(
    targetState: Int,
    content: @Composable() AnimatedVisibilityScope.(targetState: Int) -> Unit
) {
    val density = LocalDensity.current
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(initialOffsetX = { with(density) { 200.dp.roundToPx() } }) +
                        fadeIn() with
                        slideOutHorizontally(targetOffsetX = { with(density) { -200.dp.roundToPx() } }) +
                        fadeOut()
            } else {
                slideInHorizontally(initialOffsetX = { with(density) { -200.dp.roundToPx() } }) +
                        fadeIn() with
                        slideOutHorizontally(targetOffsetX = { with(density) { 200.dp.roundToPx() } }) +
                        fadeOut()
            }.using(SizeTransform(clip = false))
        },
        content = content
    )
}

@Composable
private fun ZoomableMedia(
    media: Media,
    zoom: Boolean,
    size: IntSize,
    onChange: (zoom: Boolean) -> Unit,
) {
    // メディアを一枚ずつ表示
    Box(Modifier.clickable { onChange(!zoom) }) {
        if (!zoom) {
            media.view()
        } else {
            when (media) {
                is ImageMedia -> ZoomedImage(media, size)
                else -> media.view()
            }
        }
    }
}

@Composable
private fun ZoomedImage(
    image: ImageMedia,
    size: IntSize,
) {
    val horizontalScrollState by remember { mutableStateOf(ScrollState(0)) }
    val verticalScrollState by remember { mutableStateOf(ScrollState(0)) }

    val density = LocalDensity.current.run {
        1.dp.toPx()
    }
    val wide = (size.width.toFloat() / size.height.toFloat()
            > image.asset.width.toFloat() / image.asset.height.toFloat())
    val modifier = if (wide) {
        Modifier.width(size.width.dp / density)
            .height(
                size.width.dp
                        * (image.asset.height.toFloat() / image.asset.width.toFloat())
                        / density
            )
    } else {
        Modifier.height(size.height.dp / density)
            .width(
                size.height.dp
                        * (image.asset.width.toFloat() / image.asset.height.toFloat())
                        / density
            )
    }
    Box(
        Modifier.horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
            .fillMaxSize()
    ) {
        image.view(modifier)
    }
}