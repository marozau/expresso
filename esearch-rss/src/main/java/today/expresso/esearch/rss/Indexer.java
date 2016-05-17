package today.expresso.esearch.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Created by im on 5/17/16.
 */
public interface Indexer {
    void prepare();
    void commit();

    void index(SyndFeed feed, SyndEntry entry);
}
