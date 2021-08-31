package content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

/**
 * メディアの詰まったフォルダ。
 */
class Collection(
    /**
     * フォルダの所在地
     */
    dir: Path
) : Content(dir) {
    /**
     * 内包するコレクションの一覧
     */
    val subCollections: List<Collection>
    /**
     * 内包するメディアの一覧
     */
    val mediaList: List<Media>

    /**
     * 内包するコンテンツの一覧
     */
    val subContents: ArrayList<Content>
        get() {
            val arrayList = arrayListOf<Content>()
            arrayList.addAll(subCollections)
            arrayList.addAll(mediaList)

            return arrayList
        }

    init {
        // `dir`がディレクトリ以外のものを指しているとき
        if (!dir.isDirectory()) {
            // コレクションを読み込めないのでエラーを投げる
            throw CollectionLoadException(dir)
        }

        subCollections = ArrayList(Files.newDirectoryStream(dir)
            // ディレクトリ内のエントリを全部コレクションにしてみる
            // ディレクトリでないエントリは当然エラーを返す
            .map { kotlin.runCatching { Collection(it) } }
            // コレクションにならなかったものを除外
            .filter { it.isSuccess }
            // unwrap
            .map { it.getOrThrow() })

        mediaList = ArrayList(Files.newDirectoryStream(dir)
                // ディレクトリ内のエントリを全部メディアにしてみる
                // メディアでないエントリは当然エラーを返す
                .map { kotlin.runCatching { Media.build(it) } }
                // メディアにならなかったものを除外
                .filter { it.isSuccess }
                // unwrap
                .map { it.getOrThrow() })

        if (subContents.isEmpty()) {
            // コンテンツを持たないコレクションを拒絶
            throw NoMediaCollectionException(dir)
        }
    }

    @Composable
    override fun view() {
        Card {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // サムネイルアイコン
                thumbIcon()
                // タイトル
                Text(name)
            }
        }
    }

    @Composable
    override fun thumbIcon() {
        subContents[0].thumbIcon()
    }
}

abstract class CollectionException : Exception()

/**
 * エラー: コレクションの読み込みに失敗
 */
data class CollectionLoadException(val dir: Path) : CollectionException()

/**
 * エラー: メディアのないコレクション
 */
data class NoMediaCollectionException(val dir: Path) : CollectionException()