package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.*;

public class CrawldaddyResultSet implements Iterable<CrawldaddyResult> {
    List<CrawldaddyResult> results = new ArrayList<>();
    
    public void addResult(CrawldaddyResult result) {
        if (result == null) {
            throw new NullPointerException("Can't add null CrawldaddyResult");
        }
        results.add(result);
    }

    @Override
    public Iterator<CrawldaddyResult> iterator() {
        return results.iterator();
    }
    
    public Duration getAverageCrawlTime() {
        long totalCrawlTimeMillis = 0;
        int numSuccessfulCrawls = 0;
        for (CrawldaddyResult res : results) {
            if (!res.hasPageFetchException() && res.isHttpStatusOK()) {
                Duration crawlTime = res.getCrawlTime();
                totalCrawlTimeMillis += crawlTime.toMillis();
                numSuccessfulCrawls++;
            }
        }
        return computeAverageCrawlTime(totalCrawlTimeMillis, numSuccessfulCrawls);
    }
    
    private Duration computeAverageCrawlTime(long totalCrawlTimeMillis, int numSuccessfulCrawls) {
        if (numSuccessfulCrawls == 0) {
            return Duration.ZERO;
        }
        long avgCrawlTimeMillis = totalCrawlTimeMillis / numSuccessfulCrawls;
        return Duration.ofMillis(avgCrawlTimeMillis);
    }
}
