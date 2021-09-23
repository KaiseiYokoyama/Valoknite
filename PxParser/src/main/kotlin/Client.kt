import error.ArtworkParseFailedException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

class Client private constructor(id: IllustId) {
    val artwork: Artwork
    val session = Jsoup.newSession()

    companion object {
        fun connect(id: IllustId): Result<Client> {
            return kotlin.runCatching {
                Client(id)
            }
        }
    }

    init {
        val url = "https://www.pixiv.net/artworks/$id";
        val doc = session.newRequest()
            .url(url)
            .get()
        val meta = doc.selectFirst("#meta-preload-data")
            ?: throw ArtworkParseFailedException(id, url, doc)
        val json = meta.attr("content")

        artwork = Json { ignoreUnknownKeys = true }.decodeFromString<Artwork>(json)
    }

    fun getImageAsBytes(url: String): ByteArray {
        return session.url(url)
            .referrer("https://www.pixiv.net/")
            .ignoreContentType(true)
            .execute()
            .bodyAsBytes()
    }
}