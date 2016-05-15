package today.expresso.esearch.rss;

import com.google.gson.*;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherListener;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by im on 5/15/16.
 */
public class RssFetcher {


    public static void main(final String[] args) {

        try {
            final URL feedUrl = new URL(System.getenv(RssFetcherConfig.FETCH_URL));

//            final File cachePath = new File(
//                    System.getenv(RssFetcherConfig.CACHE_DIR) +
//                            "/" +
//                            System.getenv(RssFetcherConfig.CACHE_ID));
//            cachePath.mkdir();
            final FeedFetcher fetcher = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());
            final FetcherEventListenerImpl listener = new FetcherEventListenerImpl(
                    System.getenv(RssFetcherConfig.ELASTICSEARCH_HOST),
                    System.getenv(RssFetcherConfig.ELASTICSEARCH_INDEX),
                    System.getenv(RssFetcherConfig.ELASTICSEARCH_TYPE));

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
        } catch (final Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static class FetcherEventListenerImpl implements FetcherListener {

        private static final Gson SERIALIZER = new GsonBuilder().serializeNulls().create();
        public static final DateFormat INDEX_DATE_FORMAT = new SimpleDateFormat("dd-MM-YYYY");

        private final Set<String> cache = new HashSet<>(); //todo: replace by Litewait persisten map MapDb or RocksDb

        private final MessageDigest md;

        private final Client client;

        private final String index;
        private final String type;

        public FetcherEventListenerImpl(String host, String index, String type) throws UnknownHostException, NoSuchAlgorithmException {
            this.index = index;
            this.type = type;

            this.md = MessageDigest.getInstance("MD5");

            final Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", "elasticsearch")
                    .put("client.transport.sniff", true)
                    .build();
            this.client = TransportClient.builder()
                    .settings(settings).build()
                    .addTransportAddresses(
                            new InetSocketTransportAddress(InetAddress.getByName(host), 9300));
        }

        @Override
        public void fetcherEvent(final FetcherEvent event) {
            final String eventType = event.getEventType();
            if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
                System.out.println("\tEVENT: Feed Polled. URL = " + event.getUrlString());
            } else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
                final BulkRequestBuilder bulkRequest = client.prepareBulk();
                System.out.println("\tEVENT: Feed Retrieved. URL = " + event.getUrlString() + " - " + event.getFeed().getEntries().size());
                event.getFeed().setUri(event.getUrlString());
                for (final Object object : event.getFeed().getEntries()) {
                    final SyndEntry entry = (SyndEntry) object;
                    if (cache.add(entry.getUri())) {
                        final String feedJson = SERIALIZER.toJson(new SyndEntryAdapter(event.getFeed(), entry), SyndEntryAdapter.class);
                        bulkRequest.add(client.prepareIndex(
                                index + "-" + INDEX_DATE_FORMAT.format(new Date()),
                                type,
                                new String(md.digest(entry.getUri().getBytes())))
                                .setSource(feedJson));
                    }
                }
                final BulkResponse bulkResponse = bulkRequest.get();
                if (bulkResponse.hasFailures())
                    System.err.println(bulkResponse.buildFailureMessage());
            } else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
                System.out.println("\tEVENT: Feed Unchanged. URL = " + event.getUrlString());
            }
        }
    }
}
