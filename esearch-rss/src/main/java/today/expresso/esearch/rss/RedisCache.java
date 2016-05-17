package today.expresso.esearch.rss;

import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by im on 5/17/16.
 */
public class RedisCache implements Cache {

    private final MessageDigest md;
    private final Jedis cache;

    public RedisCache(String host, String algorithm) throws NoSuchAlgorithmException {
        this.md = MessageDigest.getInstance(algorithm);
        this.cache = new Jedis(host);
    }

    @Override
    public boolean exists(String digest) {
        return cache.exists(digest);
    }

    @Override
    public void set(String digest, String value) {
        cache.set(digest, value);
    }

    @Override
    public String digest(String value) {
        return String.valueOf(Hex.encodeHex(md.digest(value.getBytes())));
    }
}
