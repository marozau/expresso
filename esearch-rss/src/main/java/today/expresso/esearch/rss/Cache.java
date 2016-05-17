package today.expresso.esearch.rss;

/**
 * Created by im on 5/17/16.
 */
public interface Cache {
    boolean exists(String digest);

    void set(String digest, String value);

    String digest(String value);
}
