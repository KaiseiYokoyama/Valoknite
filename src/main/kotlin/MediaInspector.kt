import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import content.Content
import content.ImageMedia
import content.Media
import org.jsoup.HttpStatusException
import java.awt.Desktop
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

open class MediaInspector(open val media: Media) {
    companion object {
        @Composable
        fun view(modifier: Modifier = Modifier, media: Media) {
            when (media) {
                is ImageMedia -> ImageInspector.build(media)
                else -> MediaInspector(media)
            }.view(modifier)
        }
    }

    @Composable
    protected fun headerTitle(title: String) =
        Text(title, Modifier.padding(start = 5.dp), fontSize = 15.sp, fontWeight = FontWeight(800))

    open class Property(
        val icon: ImageVector,
        val description: String,
        val content: @Composable () -> Unit,
    ) {
        @Composable
        open fun color(): Color = MaterialTheme.colors.primary

        @Composable
        fun view() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, description, tint = color())
                content()
            }
        }
    }

    data class Action(
        val icon: ImageVector,
        val description: String,
        val onClick: () -> Unit,
    ) {
        @Composable
        fun view() {
            TextButton(onClick) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(icon, description)
                    Text(description.uppercase(), fontWeight = FontWeight(600))
                }
            }
        }
    }

    @Composable
    protected open fun header() = Surface(
        color = MaterialTheme.colors.primary,
        contentColor = contentColorFor(MaterialTheme.colors.primary),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(media.name, fontSize = 20.sp)
            Text(media.mediaType(), fontSize = 15.sp)
        }
    }

    protected open fun extraActions() = mutableListOf<Action>()

    @Composable
    protected fun actions() = Column {
        val actions = listOf(
            Action(Icons.Default.OpenInNew, "Open") {
                Desktop.getDesktop().browseFileDirectory(media.path.toFile())
            },
            *extraActions().toTypedArray()
        )

        Row(
            Modifier.padding(horizontal = 5.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.forEach { action -> action.view() }
        }
    }

    protected open fun extraProperties() = mutableListOf<Property>()

    @Composable
    private fun properties() {
        val extraProperties = extraProperties()
        val properties = listOf(
            Property(Icons.Default.LocationOn, "パス") {
                Text(media.path.toString())
            },
            Property(Icons.Default.DataUsage, "サイズ") {
                Text(media.size.toString())
            },
            Property(Icons.Default.AccessTime, "更新日時") {
                Text(
                    media.lastMod
                        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
                )
            },
            *extraProperties.toTypedArray()
        )

        Column(Modifier.padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            properties.forEach { property -> property.view() }
        }
    }

    protected open fun extraComposable(): LazyListScope.() -> Unit {
        return {}
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    protected fun view(modifier: Modifier) = Surface(
        modifier.fillMaxHeight().zIndex(20f),
        elevation = 10.dp,
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            item { header() }
            item { actions() }
            item { Divider(Modifier.fillMaxWidth().padding(vertical = 5.dp)) }
            item { headerTitle("Properties") }
            item { properties() }
            item { Spacer(Modifier.height(10.dp)) }
            extraComposable()()
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

open class ImageInspector protected constructor(override val media: ImageMedia) : MediaInspector(media) {
    companion object {
        fun build(media: ImageMedia): ImageInspector = PixivIllustInspector.build(media) ?: ImageInspector(media)
    }

    override fun extraProperties(): MutableList<Property> {
        val exProps = super.extraProperties()
        exProps.add(
            0,
            Property(
                Icons.Default.Transform,
                "画像のサイズ"
            ) { Text("${media.assetSize.width} x ${media.assetSize.height}") }
        )

        return exProps
    }
}

class PixivIllustInspector(media: ImageMedia, val id: IllustId, val page: Int) : ImageInspector(media) {
    class Property(
        icon: ImageVector,
        description: String,
        content: @Composable () -> Unit
    ) : MediaInspector.Property(icon, description, content) {
        @Composable
        override fun color(): Color = Color(0x01, 0x96, 0xf9)
    }

    companion object {
        val regexPattern = Regex("""^(\d+)_p(\d+)\.(.*)""")

        fun hasValidPixivId(content: Content): Boolean {
            return regexPattern.matches(content.name)
        }

        fun build(media: ImageMedia): PixivIllustInspector? = if (!hasValidPixivId(media)) {
            null
        } else {
            val groups = regexPattern.find(media.name)!!.groups
            PixivIllustInspector(media, groups[1]!!.value, groups[2]!!.value.toInt())
        }
    }

    val pixivClient: Result<Client> = Client.connect(id)

    override fun extraActions(): MutableList<Action> {
        val actions = super.extraActions()
        actions.add(
            Action(
                Icons.Default.OpenInBrowser,
                "Pixiv",
            ) { Desktop.getDesktop().browse(URI("https://www.pixiv.net/artworks/$id")) }
        )

        return actions
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun extraComposable(): LazyListScope.() -> Unit {
        var list = mutableListOf<Property>()
        var latestPosts = @Composable {}

        pixivClient.onSuccess {
            val artwork = it.artwork
            val illust = artwork.illust[id] ?: return {}
            val user = artwork.user[illust.userId] ?: return {}

            list = mutableListOf(
                Property(
                    Icons.Default.Person,
                    "作者",
                ) { Text(user.name) },
                Property(
                    Icons.Default.Title,
                    "タイトル",
                ) { Text(illust.title) },
                Property(
                    Icons.Default.Label,
                    "タグ",
                ) {
                    val tags = illust.tags.tags.map { it.tag }.joinToString(" #", "#")
                    Text("$tags", color = Color(0x01, 0x96, 0xf9))
                },
                Property(
                    Icons.Default.Update,
                    "アップロード日",
                ) {
                    val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                    val dt = LocalDateTime.ofInstant(illust.uploadDate.toInstant(), ZoneId.systemDefault())
                    Text(dt.format(dtf))
                }
            )

            val desc = illust.descriptionDoc.toAnnotatedString()
            if (desc.isNotEmpty()) {
                list.add(
                    Property(
                        Icons.Default.Message,
                        "説明",
                    ) {
                        ClickableText(desc) { offset ->
                            desc.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let {
                                    val url = it.item
                                    Desktop.getDesktop().browse(URI(url))
                                }
                        }
                    }
                )
            }

            val latestIllusts = illust.userIllusts.toList()
                .mapNotNull { (_, v) -> v }
                .sortedByDescending { ui -> ui.updateDate }
                .toMutableList()

            latestPosts = @Composable {
                Text(
                    "${user.name}先生の最近の作品",
                    Modifier.padding(start = 5.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight(800)
                )
                LazyRow(
                    Modifier.padding(10.dp),
                    LazyListState(0,0),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(latestIllusts) { illust ->
                        Image(
                            org.jetbrains.skija.Image.makeFromEncoded(it.getImageAsBytes(illust.url)).asImageBitmap(),
                            illust.title,
                            Modifier,
                        )
                    }
                }
            }
        }
            .onFailure {
                when (it) {
                    is HttpStatusException -> if (it.statusCode == 404) {
                        list.add(
                            Property(
                                Icons.Default.Warning,
                                "NOT FOUND",
                            ) { Text("この作品は削除されたか、存在しません。") }
                        )
                    }
                    else -> {
                    }
                }
            }

        return {
            item { headerTitle("Pixiv Artwork Properties") }
            item {
                Column(Modifier.padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    list.forEach { property -> property.view() }
                }
            }
            item { latestPosts() }
            super.extraComposable()
        }
    }
}
