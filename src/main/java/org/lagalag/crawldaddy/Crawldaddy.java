package org.lagalag.crawldaddy;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Main crawler class.
 *
 */
public class Crawldaddy {
    public static final int DEFAULT_MAX_INTERNAL_LINKS = 3000;
    
    private final int maxNumInternalLinks;
    private String url;

    public Crawldaddy(String url) {
        this.url = url;
        this.maxNumInternalLinks = DEFAULT_MAX_INTERNAL_LINKS;
    }
    
    public Crawldaddy(String url, int maxNumInternalLinks) {
        this.url = url;
        this.maxNumInternalLinks = maxNumInternalLinks;
    }
    
    public Future<CrawldaddyResult> startCrawl() {
        return ForkJoinPool.commonPool().submit(new Callable<CrawldaddyResult>() {
            @Override
            public CrawldaddyResult call() throws Exception {
                CrawldaddyAction cdAction = new CrawldaddyAction(url, maxNumInternalLinks);
                // Initiate the crawl and wait for the result.
                ForkJoinPool.commonPool().invoke(cdAction);
                return cdAction.getResult();
            }
        });
    }
}
