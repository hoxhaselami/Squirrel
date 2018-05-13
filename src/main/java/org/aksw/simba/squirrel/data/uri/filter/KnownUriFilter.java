package org.aksw.simba.squirrel.data.uri.filter;

import org.aksw.simba.squirrel.data.uri.CrawleableUri;
import org.aksw.simba.squirrel.deduplication.hashing.HashValue;
import org.aksw.simba.squirrel.deduplication.hashing.impl.HashValueUriPair;

import java.util.List;

/**
 * A {@link UriFilter} that works like a blacklist filter and contains only those
 * URIs on its blacklist that the crawler already has seen before.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 */
public interface KnownUriFilter extends UriFilter {

    /**
     * Adds the given URI to the list of already known URIs. Works like calling {@link #add(CrawleableUri, long)} with the current system time.
     *
     * @param uri the URI that should be added to the list.
     * @param nextCrawlTimestamp The time at which the given URI should be crawled next.
     */
    public void add(CrawleableUri uri, long nextCrawlTimestamp);

    /**
     * Adds the given URI to the list of already known URIs together with the the time at which it has been crawled.
     *
     * @param uri       the URI that should be added to the list.
     * @param lastCrawlTimestamp the time at which the given URI has eben crawled.
     * @param nextCrawlTimestamp The time at which the given URI should be crawled next.
     */
    public void add(CrawleableUri uri, long lastCrawlTimestamp, long nextCrawlTimestamp);

    /**
     * Add the given hash value for the given uri.
     *
     * @param uri       The given uri.
     * @param hashValue The given hash value.
     */
    public void addHashValueForUri(CrawleableUri uri, HashValue hashValue);

    /**
     * Get the hash value for the given uri.
     *
     * @param uri                  The given uri.
     * @param hashValueForDecoding Used for decoding of String to {@link HashValue}.
     * @return The hash value.
     */
    public HashValue getHashValueForUri(CrawleableUri uri, HashValue hashValueForDecoding);

    /**
     * Get all uris together with their hash values.
     * @return The pairs of uris and hash values.
     */
    public List<HashValueUriPair> getAllUrisAndHashValues();

    /**
     * Close RDB connection, destroy the database.
     */
    public void close();

    /**
     * Open RDB connection, init the database.
     */
    public void open();

    /**
     * Returns all {@link CrawleableUri}s which have to be recrawled. This means their time to next crawl has passed.
     *
     * @return The outdated {@link CrawleableUri}s.
     */
    public List<CrawleableUri> getOutdatedUris();

    /**
     * count the numbers of known URIs
     * @return the number of lines in that database
     */
    public long count();
}
