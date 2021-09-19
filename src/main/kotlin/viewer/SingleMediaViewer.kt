package viewer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import content.ImageMedia
import content.Media
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
    var size by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableStateOf(false) }

    MaterialTheme(
        colors = darkColors()
    ) {
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
            Box(modifier.onSizeChanged { size = it }, contentAlignment = Alignment.Center) {
                val density = LocalDensity.current
                AnimatedContent(
                    targetState = index,
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
                ) { target ->
                    // メディアを一枚ずつ表示
                    val media = contents[index]
                    Box(Modifier.clickable { zoom = !zoom }) {
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
                    }
                }
                // FABを表示
                FloatingActionButton(
                    onClick = {
                        onViewerChange(ViewMode.Scroll, index)
                    },
                    Modifier.align(Alignment.BottomEnd).padding(16.dp),
                ) {
                    Icon(Icons.Default.ViewArray, contentDescription = "一覧表示に戻る")
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