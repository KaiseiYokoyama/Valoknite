import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import content.Content
import content.ImageMedia
import content.Media
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

    data class Property(
        val icon: ImageVector,
        val description: String,
        val content: @Composable () -> Unit,
    )

//    data class Action(
//        val icon: ImageVector,
//        val description: String,
//        val onClick: @Composable () -> Unit,
//    )

    @Composable
    protected open fun header() = Surface {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(media.name, fontSize = 20.sp)
            Text(media.mediaType(), fontSize = 15.sp)
        }
    }

//    @Composable
//    protected fun actions(extraActions: Array<Action> = arrayOf()) = Column {
//        val actions = listOf(
//            Action(Icons.Default.ContentCopy, "Copy Path") {
//                LocalClipboardManager.current.setText(
//                    AnnotatedString(media.path.toString())
//                )
//            },
//            *extraActions
//        )
//
//        Row {
//            actions.forEach { action ->
//                TextButton(
//                    onClick = { action.onClick() },
//                ) {
//                    Column {
//                        Icon(action.icon, action.description)
//                        Text(action.description)
//                    }
//                }
//            }
//        }
//    }

    protected open fun extraProperties() = mutableListOf<Property>()

    @Composable
    private fun properties() {
        val extraProperties = extraProperties()
        Column(Modifier.padding(horizontal = 10.dp)) {
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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                properties.forEach { property ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(property.icon, property.description, tint = MaterialTheme.colors.primary)
                        property.content()
                    }
                }
            }
        }
    }

    @Composable
    protected fun view(modifier: Modifier) = Surface(
        modifier.fillMaxHeight(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            header()
//        actions()
            properties()
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

    val artwork: Artwork? by lazy { Artwork.build(id) }

    override fun extraProperties(): MutableList<Property> {
        val exProps = super.extraProperties()

        val artwork = artwork ?: return exProps
        val illust = artwork.illust[id] ?: return exProps
        val user = artwork.user[illust.userId] ?: return exProps

        exProps.add(
            Property(
                Icons.Default.Person,
                "作者",
            ) { Text(user.name) }
        )

        exProps.add(
            Property(
                Icons.Default.Title,
                "タイトル",
            ) { Text(illust.title) }
        )

        return exProps
    }
}