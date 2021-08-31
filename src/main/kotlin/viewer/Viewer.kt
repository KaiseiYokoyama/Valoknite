package viewer

import androidx.compose.runtime.Composable
import content.Collection
import content.Media

/**
 * メディアを表示するビューアの基底クラス
 */
abstract class Viewer(collection: Collection) {
    /**
     * メディア一覧
     */
    val mediaList: List<Media> = collection.mediaList.toList()

    /**
     * 指定されたメディアを表示対象にする
     * @param media 表示したいメディア
     * @return 指定されたメディアがない = false
     */
    abstract fun show(media: Media): Boolean

    /**
     * ビューアをcomposeする
     */
    @Composable
    abstract fun view()
}