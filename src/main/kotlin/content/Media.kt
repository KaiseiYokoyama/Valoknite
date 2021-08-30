package content

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * 表示対象となるファイル。写真、動画など。
 * コンストラクタの代わりに`Media.build()`関数を使う。
 */
abstract class Media(val file: Path): Content(file) {
    companion object {
        /**
         * ファイルを適切なメディアのインスタンスとして扱う
         * @throws NoValidViewerException ファイルに対応するメディアのクラスがない
         */
        fun build(file: Path): Media {
            return kotlin.runCatching {
                ImageMedia(file)
            }
                .getOrThrow()
        }
    }
}

/**
 * 画像を内容とするメディア
 */
class ImageMedia constructor(file: Path) : Media(file) {
    companion object {
        /**
         * 対応する拡張子: painterResource()のドキュメントより
         */
        val extensions: Array<String> =
            arrayOf("svg", "bmp", "gif", "heif", "ico", "jpg", "jpeg", "png", "wbmp", "webp")
    }

    init {
        if (!extensions.contains(file.extension)) {
            throw NoValidViewerException(file)
        }
    }

    @Composable
    override fun view() {
        Image(
            painter = painterResource(file.toString()),
            contentDescription = file.fileName.let { it.toString() },
        )
    }

    @Composable
    override fun thumbIcon() {
        view()
    }
}

/**
 * 例外: ファイルに対する適切なビューアがないとき
 */
data class NoValidViewerException(val file: Path) : Exception() {
    val ext: String = file.extension
}