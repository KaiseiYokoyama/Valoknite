package error

import IllustId
import org.jsoup.nodes.Element

public class ArtworkParseFailedException(id: IllustId, url: String, content: Element) : RuntimeException(
    "Failed to parse the page of artwork $id\n" +
            "[URL]:$url\n" +
            "[CONTENT]:$content\n"
)