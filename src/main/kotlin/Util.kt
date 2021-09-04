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