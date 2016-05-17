package today.expresso.esearch.rss;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by im on 5/17/16.
 */
public class ElasticSearchIndexer implements Indexer {

    private static final Gson SERIALIZER = new GsonBuilder().serializeNulls().create();
    private static final DateFormat INDEX_DATE_FORMAT = new SimpleDateFormat("dd-MM-YYYY");

    private final Client client;
    private BulkRequestBuilder bulkRequest;

    private final Cache cache;

    private final String index;
    private final String type;

    public ElasticSearchIndexer(Client client, Cache cache, String index, String type) {
        this.client = client;
        this.cache = cache;

        this.index = index;
        this.type = type;
    }


    @Override
    public void prepare() {
        this.bulkRequest = client.prepareBulk().setTimeout(TimeValue.timeValueSeconds(30));
    }

    @Override
    public void commit() {
        if (bulkRequest.numberOfActions() > 0) {
            final BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures())
                throw new RuntimeException(bulkResponse.buildFailureMessage());
        }
    }

    @Override
    public void index(SyndFeed feed, SyndEntry entry) {
        final String digest = cache.digest(entry.getUri());
        if (!cache.exists(digest)) {
            final String feedJson = SERIALIZER.toJson(new SyndEntryAdapter(feed, entry), SyndEntryAdapter.class);
            bulkRequest.add(client.prepareIndex(index + "-" + INDEX_DATE_FORMAT.format(new Date()), type, digest)
                    .setSource(feedJson));
            cache.set(digest, entry.getUri());
        }
    }
}
