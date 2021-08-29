import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import media.Media
import java.nio.file.Path
import kotlin.io.path.isDirectory
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
    private val mediaList: ArrayList<Media>

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
            dir.toList()
                // ディレクトリ内のエントリを全部コレクションにしてみる
                // ディレクトリでないエントリは当然エラーを返す
                .map { kotlin.runCatching { Collection(it) } }
                // コレクションにならなかったものを除外
                .filter { it.isSuccess }
                // unwrap
                .map { it.getOrThrow() }
        )

        mediaList = ArrayList(
            dir.toList()
                // ディレクトリ内のエントリを全部メディアにしてみる
                // メディアでないエントリは当然エラーを返す
                .map { kotlin.runCatching { Media.build(it) } }
                // メディアにならなかったものを除外
                .filter { it.isSuccess }
                // unwrap
                .map { it.getOrThrow() }
        )

        if (mediaList.isEmpty()) {
            // メディアを持たないコレクションを拒絶
            throw NoMediaCollectionException(dir)
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
            // プレビュー表示
            mediaList[0].view()
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