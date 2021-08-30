package content

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
}