package today.expresso.esearch.rss;

import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;

import java.net.URL;
import java.util.Date;

/**
 * Created by im on 5/15/16.
 */
public class RssFetcherTest {

    public static void main(final String[] args) {

        try {
//            "http://feeds.bbci.co.uk/news/rss.xml"
            final URL feedUrl = new URL("http://feeds.washingtonpost.com/rss/homepage");
            final FeedFetcher fetcher = new HttpURLFeedFetcher();

            fetcher.addFetcherEventListener(new FetcherListener() {
                @Override
                public void fetcherEvent(FetcherEvent event) {
                    System.out.println(event.getFeed());
                }
            });
            fetcher.retrieveFeed(feedUrl);

        } catch (final Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}