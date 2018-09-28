package org.lagalag.crawldaddy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Main crawler class.
 *
 */
public class Crawldaddy {
    private String url;
    private ForkJoinPool werkers = new ForkJoinPool();
    
    public Crawldaddy(String url) {
        this.url = url;
    }
    
    public Future<CrawldaddyResult> startCrawl() {
        return werkers.submit(new CrawldaddyTask(url));
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
                CrawldaddyResult result = fresult.get();
                System.out.println("RESULTS: ");
                System.out.println("Total number of links : " + result.getTotalLinkCount());
                System.out.println("Number of broken links: " + result.getBrokenLinks().size());
                System.out.println("Number of ext scripts : " + result.getExternalScripts().size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
