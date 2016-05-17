package today.expresso.esearch.rss;

import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import org.apache.commons.codec.binary.Hex;

import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by im on 5/15/16.
 */
public class RssFetcherTest {

    public static void main(final String[] args) {

        try {
            final URL feedUrl = new URL("http://www.businessinsider.com/rss");
//            final URL feedUrl = new URL("http://feeds.washingtonpost.com/rss/homepage");
            final FeedFetcher fetcher = new HttpURLFeedFetcher();

            fetcher.addFetcherEventListener(event -> System.out.println(event.getFeed()));
            fetcher.retrieveFeed(feedUrl);

            MessageDigest md = MessageDigest.getInstance("MD5");
            System.out.println(String.valueOf(Hex.encodeHex(md.digest("sdfaa".getBytes()))));

        } catch (final Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}