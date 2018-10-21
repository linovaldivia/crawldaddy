package org.lagalag.crawldaddy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main crawler class.
 *
 */
public class Crawldaddy {
    private static final Logger log = LogManager.getLogger();

    private static final int MAX_WAIT_SECS_BETWEEN_CRAWLS = 5;
    
    private CrawldaddyParams params;
    
    public Crawldaddy(CrawldaddyParams params) {
        this.params = params;
    }
    
    public CompletableFuture<CrawldaddyResultSet> crawl() {
        return CompletableFuture.supplyAsync(this::doCrawl);
    }
    
    public CompletableFuture<CrawldaddyResult> crawlOnce() {
        return CompletableFuture.supplyAsync(this::doSingleCrawl);
    }
    
    private CrawldaddyResultSet doCrawl() {
        CrawldaddyResultSet results = new CrawldaddyResultSet();
        int numCrawlsToPerform = params.getNumRepetitions();
        log.debug("Performing crawl " + numCrawlsToPerform + " time(s)");
        for (int pass = 0; pass < numCrawlsToPerform; pass++) {
            if (pass > 0) {
                waitArbitrarilyBeforeProceeding();
            }
            CrawldaddyResult result = doSingleCrawl();
            results.addResult(result);
        }
        return results;
    }
    
    private CrawldaddyResult doSingleCrawl() {
        CrawldaddyAction cdAction = new CrawldaddyAction(params);
        // Invoke the action to perform the crawl and wait for the result.
        ForkJoinPool.commonPool().invoke(cdAction);
        return cdAction.getResult();
    }
    
    private void waitArbitrarilyBeforeProceeding() {
        int numSecsToWait = getArbitraryWaitTimeSeconds();
        try {
            log.info("Waiting " + numSecsToWait + " second(s) prior to next crawl...");
            TimeUnit.SECONDS.sleep(numSecsToWait);
        } catch (InterruptedException e) {
            // Thread sleep interrupted, but let's proceed anyway.
        }
    }
    
    private int getArbitraryWaitTimeSeconds() {
        return ThreadLocalRandom.current().nextInt(MAX_WAIT_SECS_BETWEEN_CRAWLS) + 1;
    }
}
