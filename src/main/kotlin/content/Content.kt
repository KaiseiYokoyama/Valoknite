package content

import Size
import androidx.compose.runtime.Composable
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.getLastModifiedTime
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
     * コレクションのサイズ
     */
    val size: Size by lazy { size() }

    /**
     * コレクションの最終更新日
     */
    val lastMod: LocalDateTime by lazy { lastMod() }

    /**
     * コンテンツを表示
     */
    @Composable
    abstract fun view()

    private fun size(): Size {
        fun Path.size(): Long {
            return if (this.isDirectory()) {
                Files.newDirectoryStream(this).sumOf { it.size() }
            } else {
                Files.size(this)
            }
        }

        return Size(path.size())
    }

    private fun lastMod(): LocalDateTime {
        val time = path.getLastModifiedTime().toInstant()

        return LocalDateTime.ofInstant(time, ZoneId.systemDefault())
    }
}