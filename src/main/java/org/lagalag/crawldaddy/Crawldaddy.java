package org.lagalag.crawldaddy;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Main crawler class.
 *
 */
public class Crawldaddy {
    private CrawldaddyParams params;
    
    public Crawldaddy(CrawldaddyParams params) {
        this.params = params;
    }
    
    public Future<CrawldaddyResult> startCrawl() {
        return ForkJoinPool.commonPool().submit(new Callable<CrawldaddyResult>() {
            @Override
            public CrawldaddyResult call() throws Exception {
                CrawldaddyAction cdAction = new CrawldaddyAction(params);
                // Initiate the crawl and wait for the result.
                ForkJoinPool.commonPool().invoke(cdAction);
                return cdAction.getResult();
            }
        });
    }
}
