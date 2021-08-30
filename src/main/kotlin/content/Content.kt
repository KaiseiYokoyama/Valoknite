package content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import java.nio.file.Path
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

    /**
     * コンテンツのサムネイルに使うアイコン
     */
    @Composable
    abstract fun thumbIcon()

    /**
     * サムネイル表示
     */
    @Composable
    fun viewAsThumbnail() {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // サムネイルアイコン
            thumbIcon()
            // フォルダ名
            Text(name)
        }
    }
}