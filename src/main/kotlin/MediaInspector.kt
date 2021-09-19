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
import content.ImageMedia
import content.Media
import java.time.format.DateTimeFormatter

open class MediaInspector(open val media: Media) {
    companion object {
        @Composable
        fun view(modifier: Modifier = Modifier, media: Media) {
            when (media) {
                is ImageMedia -> ImageInspector(media)
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

    @Composable
    protected fun properties(extraProperties: MutableList<Property>) =
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

    @Composable
    protected open fun properties() = properties(mutableListOf())

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

class ImageInspector(override val media: ImageMedia) : MediaInspector(media) {
    @Composable
    override fun properties() {
        val extraProperties = mutableListOf(
            Property(
                Icons.Default.Transform,
                "画像のサイズ"
            ) { Text("${media.assetSize.width} x ${media.assetSize.height}") }
        )
        super.properties(extraProperties)
    }
}