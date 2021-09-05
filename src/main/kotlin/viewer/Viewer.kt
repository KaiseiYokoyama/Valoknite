package viewer

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    // メディア切り替え関係
    val reqr = FocusRequester()
    LaunchedEffect(Unit) { reqr.requestFocus() }

    // アニメーション


    MaterialTheme(
        colors = darkColors()
    ) {
        Surface(Modifier.onKeyEvent {
            if (it.type != KeyEventType.KeyDown) {
                false
            } else {
                when (it.key) {
                    Key.Escape -> {
                        onViewerChange(ViewMode.Scroll, index)
                        true
                    }
                    Key.DirectionLeft -> {
                        val newIndex = kotlin.math.max(index - 1, 0)
                        if (index != newIndex) {
                            index = newIndex
                            zoom = false
                        }
                        true
                    }
                    Key.DirectionRight -> {
                        val newIndex = kotlin.math.min(index + 1, contents.size - 1)
                        if (index != newIndex) {
                            index = newIndex
                            zoom = false
                        }
                        true
                    }
                    else -> false
                }
            }
        }
            .focusRequester(reqr)
            .focusable()
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
    val index = target
    val scrollState by remember { mutableStateOf(LazyListState(index, 0)) }

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