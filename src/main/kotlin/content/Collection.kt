package content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import content.Media
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * メディアの詰まったフォルダ。
 */
class Collection(
    /**
     * フォルダの所在地
     */
    val dir: Path
) {
    /**
     * コレクション直下に存在するコレクション
     */
    private val subCollections: ArrayList<Collection>

    /**
     * コレクション直下に存在するメディア
     */
    val mediaList: ArrayList<Media>

    /**
     * コレクション名( = フォルダ名)
     */
    val name: String
        get() {
            return dir.name
        }

    init {
        // `dir`がディレクトリ以外のものを指しているとき
        if (!dir.isDirectory()) {
            // コレクションを読み込めないのでエラーを投げる
            throw CollectionLoadException(dir)
        }

        subCollections = ArrayList(
            dir.listDirectoryEntries()
                // ディレクトリ内のエントリを全部コレクションにしてみる
                // ディレクトリでないエントリは当然エラーを返す
                .map { kotlin.runCatching { Collection(it) } }
                // コレクションにならなかったものを除外
                .filter { it.isSuccess }
                // unwrap
                .map { it.getOrThrow() }
        )

        mediaList = ArrayList(
            dir.listDirectoryEntries()
                // ディレクトリ内のエントリを全部メディアにしてみる
                // メディアでないエントリは当然エラーを返す
                .map { kotlin.runCatching { Media.build(it) } }
                // メディアにならなかったものを除外
                .filter { it.isSuccess }
                // unwrap
                .map { it.getOrThrow() }
        )

        if (mediaList.isEmpty() && subCollections.isEmpty()) {
            // メディアおよびコレクションを持たないコレクションを拒絶
            throw NoMediaCollectionException(dir)
        }
    }

    /**
     * 画像
     */
    @Composable
    fun image() {
        if (mediaList.isNotEmpty()) {
            mediaList[0].view()
        } else {
            subCollections[0].image()
        }
    }

    /**
     * サムネイル表示
     */
    @Composable
    fun viewAsThumbnail() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // サムネイル画像
            image()
            // フォルダ名
            Text(name)
        }
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