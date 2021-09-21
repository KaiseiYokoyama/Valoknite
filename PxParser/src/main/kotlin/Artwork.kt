import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

typealias UserId = String
typealias IllustId = String

@Serializable
data class Artwork(
    @Serializable(with = DateSerializer::class)
    val timestamp: Date,
    val illust: Map<IllustId, Illust>,
    val user: Map<UserId, User>,
) {
    companion object {
        fun build(id: IllustId): Artwork? {
            val doc = Jsoup.connect("https://www.pixiv.net/artworks/$id").get()
            val meta = doc.selectFirst("#meta-preload-data") ?: return null
            val json = meta.attr("content")

            return Json { ignoreUnknownKeys = true }.decodeFromString<Artwork>(json)
        }
    }
}

@Serializable
data class User(
    val userId: UserId,
    val name: String,
    val image: String,
    val premium: Boolean,
    val acceptRequest: Boolean,
)

@Serializable
data class Illust(
    val id: IllustId,
    val title: String,
    val description: String,
    @Serializable(with = DateSerializer::class)
    val createDate: Date,
    @Serializable(with = DateSerializer::class)
    val uploadDate: Date,
    val tags: Tags,
    val userId: UserId,
    val userName: String,
    val userAccount: String,
    val userIllusts: Map<IllustId, UserIllust?>,
    val bookmarkCount: Int,
    val likeCount: Int,
    val viewCount: Int,
) {
    val descriptionDoc: Element by lazy {
        Jsoup.parseBodyFragment(description).body()
    }
}

@Serializable
data class UserIllust(
    val id: String,
    val title: String,
    val url: String,
    val tags: List<String>,
    val userId: UserId, /* = kotlin.String */
    val userName: String,
    val pageCount: Int,
    @Serializable(with = DateSerializer::class)
    val createDate: Date,
    @Serializable(with = DateSerializer::class)
    val updateDate: Date,
)

@Serializable
data class Tags(
    val authorId: String,
    val isLocked: Boolean,
    val tags: List<Tag>,
)

@Serializable
data class Tag(
    val tag: String,
    val locked: Boolean,
    val deletable: Boolean,
)

class DateSerializer : KSerializer<Date> {
    // ISO 8601
    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    override val descriptor: SerialDescriptor by lazy {
        PrimitiveSerialDescriptor(
            DateSerializer::class.qualifiedName!!,
            PrimitiveKind.STRING
        )
    }

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(df.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())
    }
}