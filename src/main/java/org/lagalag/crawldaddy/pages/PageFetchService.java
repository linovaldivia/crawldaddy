package org.lagalag.crawldaddy.pages;

/**
 * Identifies a provider of a service that fetches and parses a web page given its URL.
 *
 */
public interface PageFetchService {
    void fetch(String url, PageFetchConsumer resultsConsumer) throws PageFetchException;
}
