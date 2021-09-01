package viewer

import OrderBy
import androidx.compose.runtime.Composable
import content.Collection
import content.Media

enum class ViewMode {
    Single, Scroll,
}

/**
 * メディアを表示するビューアの基底クラス
 */
abstract class Viewer(collection: Collection, private var orderBy: OrderBy) {
    /**
     * メディア一覧
     */
    val mediaList: ArrayList<Media> = ArrayList(collection.mediaList.sortedWith(orderBy.sorter))

    /**
     * 指定されたメディアを表示対象にする
     * @param media 表示したいメディア
     * @return 指定されたメディアがない = false
     */
    abstract fun show(media: Media): Boolean

    protected open fun orderBy(newOrderBy: OrderBy) {
        orderBy = newOrderBy
        mediaList.sortWith(orderBy.sorter)
    }

    /**
     * ビューアをcomposeする
     */
    @Composable
    abstract fun view(onViewerChange: (ViewMode, Media) -> Unit, orderBy: OrderBy)
}