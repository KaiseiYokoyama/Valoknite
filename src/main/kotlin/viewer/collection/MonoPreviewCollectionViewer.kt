package viewer.collection

import OrderBy
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import content.Collection
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import viewer.ViewMode
import java.time.format.DateTimeFormatter

/**
 * コレクションのプレビュー
 */
@Composable
private fun Preview(collection: Collection) {
    Column {
        // コレクションのプレビュー
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            collection.view()
        }
        // コレクションの詳細
        val scrollState = rememberScrollState()
        Column(
            Modifier.weight(1f).verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // コレクションネーム
            Text(collection.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)

            @Composable
            fun Detail(title: String, detail: String) = Column {
                Text(title, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(detail, fontSize = 12.sp)
            }

            // パス
            Detail("Path", collection.path.toString())
            // メディア数
            Detail("Medias", collection.mediaList.size.toString())
            // サイズ
            Detail("Size", collection.size.toString())
            // 最終更新
            Detail(
                "Last Modified Date",
                collection.lastMod.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
            )
        }
    }
}

@Composable
private fun ListHeader(
    modifier: Modifier,
    orderBy: OrderBy,
    selectOrder: (newOrderBy: OrderBy) -> Unit,
) {
    @Composable
    fun HeaderIdem(modifier: Modifier, by: OrderBy.By) {
        val selected = orderBy.by == by
        Row(
            modifier.clickable {
                val newOrder = if (selected) {
                    orderBy.flipped()
                } else {
                    OrderBy.DEFAULT.copy(by = by)
                }
                selectOrder(newOrder)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                by.toString(),
                Modifier.weight(3f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            if (selected) {
                orderBy.order.icon()
            }
        }
    }
    Surface {
        Row(modifier.height(30.dp)) {
            HeaderIdem(Modifier.weight(3f).fillMaxHeight(), OrderBy.By.Name)
            HeaderIdem(Modifier.weight(1f).fillMaxHeight(), OrderBy.By.Date)
            HeaderIdem(Modifier.weight(1f).fillMaxHeight(), OrderBy.By.Size)
        }
    }
}

@Composable
private fun ListItem(
    modifier: Modifier,
    collection: Collection,
    selected: Boolean,
    onClick: (Collection, selected: Boolean) -> Unit,
) {
    val color = if (selected) {
        Color.Blue
    } else {
        MaterialTheme.colors.surface
    }
    val contentColor = if (selected) {
        Color.White
    } else {
        MaterialTheme.colors.onSurface
    }

    Surface(
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier.clickable { onClick(collection, selected) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                collection.name,
                Modifier.weight(3f, true),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                collection.lastMod.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")),
                Modifier.weight(1f, true),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                collection.size.toString(),
                Modifier.weight(1f, true),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 左側に選択されたコレクションのプレビュー、
 * 右側にコレクションのリストを表示するビューア
 */
@OptIn(ExperimentalFoundationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun MonoPreviewCollectionViewer(
    modifier: Modifier = Modifier.fillMaxSize(),
    contents: List<Collection>,
    onClickCollection: (Collection) -> Unit,
    onViewerChange: (ViewMode) -> Unit,
    orderBy: OrderBy,
    onOrderChange: (OrderBy) -> Unit,
) {
    var target by remember { mutableStateOf(0) }

    // フォーカスを要求
    val reqr = FocusRequester()
    LaunchedEffect(Unit) { reqr.requestFocus() }

    val onClickCollection = { collection: Collection ->
        target = 0
        onClickCollection(collection)
    }
    val onOrderChange = { orderBy: OrderBy ->
        target = 0
        onOrderChange(orderBy)
    }

    MaterialTheme(
        colors = lightColors()
    ) {
        Surface {
            Box {
                Row(modifier.padding(8.dp)) {
                    // 左：プレビュー
                    Box(Modifier.weight(1f, true)) {
                        Preview(contents[target])
                    }
                    // 右：コレクションリスト
                    val scrollState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()
                    LazyColumn(
                        Modifier.weight(3f, true)
                            .onKeyEvent {
                                if (it.type == KeyEventType.KeyDown) {
                                    val diffIndex = { diff: Int ->
                                        target = minOf(maxOf(target + diff, 0), contents.size - 1)
                                    }
                                    when (it.key) {
                                        Key.DirectionDown -> diffIndex(1)
                                        Key.DirectionUp -> diffIndex(-1)
                                        Key.Enter -> onClickCollection(contents[target])
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            .focusRequester(reqr)
                            .focusable(),
                        state = scrollState
                    ) {
                        stickyHeader {
                            ListHeader(Modifier.weight(1f), orderBy) {
                                onOrderChange(it)
                                coroutineScope.launch { scrollState.scrollToItem(0) }
                            }
                        }
                        itemsIndexed(contents) { idx, content ->
                            ListItem(Modifier.height(20.dp), content, idx == target) { collection, selected ->
                                if (selected) { // 選択済みのアイテムであれば、そのコレクションを開く
                                    onClickCollection(collection)
                                } else { // 選択前のアイテムであれば、そのコレクションを選択
                                    target = idx
                                }
                            }
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
    }
}