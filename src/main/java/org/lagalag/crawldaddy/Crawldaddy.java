package org.lagalag.crawldaddy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Main crawler class.
 *
 */
public class Crawldaddy {
    private String url;
    
    public Crawldaddy(String url) {
        this.url = url;
    }
    
    public Future<CrawldaddyResult> startCrawl() {
        return ForkJoinPool.commonPool().submit(new Callable<CrawldaddyResult>() {
            @Override
            public CrawldaddyResult call() throws Exception {
                CrawldaddyAction cdAction = new CrawldaddyAction(url);
                ForkJoinPool.commonPool().invoke(cdAction);
                return cdAction.getResult();
            }
        });
    }
    
    private static void showResults(CrawldaddyResult result) {
        System.out.println("RESULTS: ");
        System.out.println("Total number of links : " + result.getTotalLinkCount());
        System.out.println("Number of broken links: " + result.getBrokenLinks().size());
        System.out.println("Number of ext scripts : " + result.getExternalScripts().size());
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: crawldaddy <url-to-crawl>");
            System.exit(1);
        }
        
        String url = args[0];
        System.out.println("Crawling " + url + "...");
        Future<CrawldaddyResult> fresult = new Crawldaddy(url).startCrawl();
        if (fresult != null) {
            try {
                showResults(fresult.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
