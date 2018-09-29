package org.lagalag.crawldaddy;

import java.util.Set;
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
                // Initiate the crawl and wait for the result.
                ForkJoinPool.commonPool().invoke(cdAction);
                return cdAction.getResult();
            }
        });
    }
    
    private static void showResults(CrawldaddyResult result) {
        if (result == null) {
            return;
        }
        Set<String> allLinks = result.getAllLinks();
        Set<String> extLinks = result.getExternalLinks();
        Set<String> brokenLinks = result.getBrokenLinks();
        Set<String> extScripts = result.getExternalScripts();
        
        System.out.println("RESULTS:");
        System.out.println("Total number of unique links : " + allLinks.size());
        System.out.println("   Number of external links  : " + extLinks.size());
        System.out.println("Number of broken links       : " + brokenLinks.size());
        System.out.println("Number of ext scripts        : " + extScripts.size());
        showSetContents("EXTERNAL LINKS", extLinks);
        showSetContents("BROKEN LINKS", brokenLinks);
        showSetContents("EXTERNAL SCRIPTS", extScripts);
    }
    
    private static void showSetContents(String title, Set<String> set) {
        if (set.size() > 0) {
            System.out.println(title + " (" + set.size() + "): ");
            for (String s : set) {
                System.out.println(s);
            }
        }
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
