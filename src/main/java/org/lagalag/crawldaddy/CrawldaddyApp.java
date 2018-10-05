package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Main command-line application class: handles command-line parsing, displaying output, etc.
 *
 */
public class CrawldaddyApp {
    
    private void showResults(CrawldaddyResult result, CrawldaddyCommandLine commandLine) {
        if (result == null) {
            return;
        }
        Set<String> extLinks = result.getExternalLinks();
        Set<String> brokenLinks = result.getBrokenLinks();
        Set<String> extScripts = result.getExternalScripts();
        
        System.out.println("RESULTS for " + result.getUrl() + ":");
        System.out.println("Total number of unique links : " + result.getTotalLinkCount());
        System.out.println("   Number of external links  : " + extLinks.size());
        System.out.println("Number of broken links       : " + brokenLinks.size());
        System.out.println("Number of ext scripts        : " + extScripts.size());
        if (commandLine.isShowExternalLinksSet()) {
            showSetContents("EXTERNAL LINKS", extLinks);
        }
        showSetContents("BROKEN LINKS", brokenLinks);
        if (commandLine.isShowExternalScriptsSet()) {
            showSetContents("EXTERNAL SCRIPTS", extScripts);
        }
        showCrawlTime(result.getCrawlTime());
    }
    
    private void showSetContents(String title, Set<String> set) {
        if (set.size() > 0) {
            System.out.println(title + " (" + set.size() + "): ");
            for (String s : set) {
                System.out.println(s);
            }
        }
    }
    
    private void showCrawlTime(Duration crawlTime) {
        StringBuilder ct = new StringBuilder("Total crawl time: ");
        if (crawlTime.toHours() > 0) {
            ct.append(crawlTime.toHours()).append(" hour(s) ");
        } else if (crawlTime.toMinutes() > 0 ) {
            ct.append(crawlTime.toMinutes()).append(" minute(s) ");
        } 
        long millis = crawlTime.toMillis();
        long secs =  millis / 1000;
        millis %= 1000;
        if (secs > 0) {
            ct.append(secs).append(".").append(millis).append(" second(s) ");
        }
        System.out.println(ct.toString());
    }
    
    private CrawldaddyParams createParams(CrawldaddyCommandLine commandLine) {
        if (commandLine == null) {
            return null;
        }
        
        CrawldaddyParams params = new CrawldaddyParams(commandLine.getInputUrl());
        params.setMaxInternalLinks(commandLine.getMaxInternalLinks());
        
        return params;
    }
    
    public void runApp(String[] args) {
        CrawldaddyCommandLine commandLine = CrawldaddyCommandLine.parse(args);
        if (commandLine == null) {
            CrawldaddyCommandLine.showHelp(System.out);
            return;
        }
        
        CrawldaddyParams params = createParams(commandLine);
        System.out.println("Crawling " + params.getUrl() + "...");
        Future<CrawldaddyResult> fresult = new Crawldaddy(params).startCrawl();
        if (fresult != null) {
            try {
                CrawldaddyResult result = fresult.get();
                showResults(result, commandLine);
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted exception: " + e.getMessage());
            } catch (ExecutionException e) {
                System.err.println("Unable to proceed: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        new CrawldaddyApp().runApp(args);
    }
}
