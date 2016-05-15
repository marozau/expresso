package today.expresso.esearch.rss;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by im on 5/15/16.
 */
public class SyndEntryAdapter {

    public static final DateFormat ISO_8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private String title = null;
    private String titleType = null;

    private String description = null;
    private String descriptionType = null;

    private String uri;
    private String author;
    private String link;

    private String publishedDate;
    private String updatedDate = null;
    private String fetchDate;

    private String sourceLink;
    private String sourceUri;
    private String sourceTitle;
    private String sourceType;
    private String language;

    public SyndEntryAdapter(SyndFeed feed, SyndEntry entry) {
        if (entry.getTitleEx() != null) {
            this.title = entry.getTitle();
            this.titleType = entry.getTitleEx().getType();
        }
        if (entry.getDescription() != null) {
            this.description = entry.getDescription().getValue();
            this.descriptionType = entry.getDescription().getType();
        }
        this.uri = entry.getUri();
        this.author = entry.getAuthor();
        this.link = entry.getLink();

        if (entry.getPublishedDate() != null)
            this.publishedDate = ISO_8601_DATE_FORMAT.format(entry.getPublishedDate());
        if (entry.getUpdatedDate() != null)
            this.updatedDate = ISO_8601_DATE_FORMAT.format(entry.getUpdatedDate());
        this.fetchDate = ISO_8601_DATE_FORMAT.format(new Date());

        this.sourceLink = feed.getLink();
        this.sourceUri = feed.getUri();
        this.sourceTitle = feed.getTitle();
        this.language = feed.getLanguage();
        this.sourceType = feed.getFeedType();
    }
}
