package org.lagalag.crawldaddy.pages;

import java.util.function.Consumer;

/**
 * Identifies a consumer of the results of the page fetch/parse operation.
 *
 */
public interface PageFetchConsumer extends Consumer<PageFetchResults> {
    void handlePageFetchResults(PageFetchResults results);
    
    @Override
    default void accept(PageFetchResults results) {
        handlePageFetchResults(results);
    }
}
