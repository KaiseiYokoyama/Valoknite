import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLDecoder
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * 情報量
 */
data class Size(public val value: Long) {
    companion object {
        private val units: Array<String> = arrayOf("B", "KB", "MB", "GB", "TB")
    }

    override fun toString(): String {
        val digit = (log10(value.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#")
            .format(value / 1024.0.pow(digit.toDouble())) + " ${units[digit]}"
    }
}

fun Element.toAnnotatedString(): AnnotatedString {
    val element = this

    return buildAnnotatedString {
        element.childNodes().forEach {
            when (it) {
                is Element -> {
                    val text = it.wholeText()
                    when (it.nodeName()) {
                        "a" -> {
                            pushStyle(SpanStyle(Color(61, 118, 153)))
                            append(text)
                            addStringAnnotation(
                                "URL",
                                URLDecoder.decode(
                                    it.attr("href")
                                        .replace("/jump.php?", ""),
                                    "UTF-8"
                                ),
                                this.length - text.length, this.length
                            )
                            pop()
                        }
                        "br" -> {
                            append('\n')
                        }
                        else -> {
                            append(text)
                        }
                    }
                }
                is TextNode -> {
                    append(it.wholeText)
                }
            }
        }
    }
}