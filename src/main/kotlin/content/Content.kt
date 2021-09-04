package content

import Size
import androidx.compose.runtime.Composable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/**
 * コンテンツ。メディアとコレクションの2種類に別れる。
 */
abstract class Content(
    /**
     * コンテンツの所在地
     */
    val path: Path
) {
    /**
     * コンテンツ名
     */
    val name: String
        get() {
            return path.name
        }

    /**
     * コンテンツを表示
     */
    @Composable
    abstract fun view()

    fun size(): Size {
        fun Path.size(): Long {
            return if (this.isDirectory()) {
                Files.newDirectoryStream(this).sumOf { it.size() }
            } else {
                Files.size(this)
            }
        }

        return Size(path.size())
    }
}