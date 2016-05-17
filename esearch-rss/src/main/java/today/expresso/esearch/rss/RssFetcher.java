package today.expresso.esearch.rss;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.URL;
import java.util.Date;

/**
 * Created by im on 5/15/16.
 */
public class RssFetcher {

    public static void main(final String[] args) throws Exception {

        final Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "elasticsearch")
                .put("client.transport.sniff", true)
                .build();
        final Client client = TransportClient.builder()
                .settings(settings).build()
                .addTransportAddresses(
                        new InetSocketTransportAddress(
                                InetAddress.getByName(System.getenv(RssFetcherConfig.ELASTICSEARCH_HOST)), 9300));

        final Cache cache = new RedisCache(System.getenv(RssFetcherConfig.CACHE), "MD5");
        final Indexer indexer = new ElasticSearchIndexer(
                client,
                cache,
                System.getenv(RssFetcherConfig.ELASTICSEARCH_INDEX),
                System.getenv(RssFetcherConfig.ELASTICSEARCH_TYPE));

        final URL feedUrl = new URL(System.getenv(RssFetcherConfig.FETCH_URL));
        final FetcherEventListenerImpl listener = new FetcherEventListenerImpl(indexer);
        final FeedFetcher fetcher = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());
        fetcher.addFetcherEventListener(listener);

        final long sleepDuration = Long.valueOf(System.getenv(RssFetcherConfig.FETCH_INTERVAL));
        while (true) {
            System.out.println(new Date(System.currentTimeMillis()).toString() + " - " + "Retrieving feed " + feedUrl);
            fetcher.retrieveFeed(feedUrl);
            System.out.println(new Date(System.currentTimeMillis()).toString() + " - " + feedUrl + " retrieved");
            try {
                Thread.sleep(sleepDuration);
            } catch (Exception ignored) {}
        }
    }

    public static class FetcherEventListenerImpl implements FetcherListener {

        private final Indexer indexer;

        public FetcherEventListenerImpl(Indexer indexer) {
            this.indexer = indexer;
        }

        @Override
        public void fetcherEvent(final FetcherEvent event) {
            final String eventType = event.getEventType();
            if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
                System.out.println("\tEVENT: Feed Polled. URL = " + event.getUrlString());
            } else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
                System.out.println("\tEVENT: Feed Retrieved. URL = " + event.getUrlString() + " - " + event.getFeed().getEntries().size());
                event.getFeed().setUri(event.getUrlString());

                indexer.prepare();
                for (final Object object : event.getFeed().getEntries()) {
                    final SyndEntry entry = (SyndEntry) object;
                    indexer.index(event.getFeed(), entry);
                }
                indexer.commit();
            } else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
                System.out.println("\tEVENT: Feed Unchanged. URL = " + event.getUrlString());
            }
        }
    }

}
