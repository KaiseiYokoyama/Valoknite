import androidx.compose.ui.awt.ComposeWindow
import java.nio.file.Path
import javax.swing.JFileChooser

/**
 * メディアビューアの振る舞いを定義するクラス。
 * @constructor コレクションを直接代入してインスタンス化する
 */
class Viewer(collection: Collection) {
    /**
     * ファイルダイアログを表示して、ユーザにコレクションとして扱うフォルダを選んでもらう
     */
    constructor(composeWindow: ComposeWindow) : this(kotlin.run {
        var dirPath: Path?
        val dialog = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }

        do {
            // ダイアログを表示
            dialog.showOpenDialog(composeWindow)
            dirPath = dialog.selectedFile.let {
                it.toPath()
            }
        } while (dirPath == null)

        Collection(dirPath)
    })

    /**
     * 表示中のコレクション
     */
    var focusOn: Collection = collection

    /**
     * コレクションの表示履歴
     */
    val history: ArrayList<Collection> = arrayListOf(collection)


}