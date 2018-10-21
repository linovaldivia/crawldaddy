package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.Set;

import org.lagalag.crawldaddy.pages.PageFetchException;

/**
 * Main command-line application class: handles command-line parsing, displaying output, etc.
 *
 */
public class CrawldaddyApp {
    public void runApp(String[] args) {
        CrawldaddyCommandLine commandLine = CrawldaddyCommandLine.parse(args);
        if (commandLine == null) {
            CrawldaddyCommandLine.showHelp(System.out);
            return;
        }
        
        CrawldaddyParams params = createParams(commandLine);
        if (params == null) {
            CrawldaddyCommandLine.showHelp(System.out);
            return;
        }
        
        startCrawl(params, commandLine);
    }
    
    private void startCrawl(CrawldaddyParams params, CrawldaddyCommandLine commandLine) {
        System.out.println("Crawling " + params.getUrl() + "...");
        Crawldaddy crawler = new Crawldaddy(params);
        try {
            CrawldaddyResultSet results = crawler.crawl().get();
            processResults(results, commandLine);
        } catch (Exception e) {
            System.err.println("A problem was encountered during the crawling operation: " + e.getMessage());
        }
    }
    
    private CrawldaddyParams createParams(CrawldaddyCommandLine commandLine) {
        CrawldaddyParams params = new CrawldaddyParams(commandLine.getInputUrl());
        params.setMaxInternalLinks(commandLine.getMaxInternalLinks(CrawldaddyParams.DEFAULT_MAX_INTERNAL_LINKS));
        params.setShowVisitedLink(commandLine.isGenerateVerboseOutputSet());
        params.setNumRepetitions(commandLine.getNumRepetitions(CrawldaddyParams.DEFAULT_CRAWL_REPETITIONS));
        return params;
    }
    
    private void processResults(CrawldaddyResultSet results, CrawldaddyCommandLine commandLine) {
        for (CrawldaddyResult result : results) {
            processResult(result, commandLine);
        }
    }
    
    private void processResult(CrawldaddyResult result, CrawldaddyCommandLine commandLine) {
        if (result.hasPageFetchException()) {
            PageFetchException e = result.getPageFetchException();
            System.err.println(e.getMessage());
        } else if (!result.isHttpStatusOK()) {
            System.err.println("Crawl attempt resulted in HTTP " + result.getHttpStatusCode());
        } else {
            Set<String> extLinks = result.getExternalLinks();
            Set<String> brokenLinks = result.getBrokenLinks();
            Set<String> extScripts = result.getExternalScripts();
            
            System.out.println("RESULTS for " + result.getUrl() + ":");
            System.out.println("Total number of unique links : " + result.getTotalLinkCount());
            System.out.println("   Number of internal links  : " + result.getInternalLinkCount());
            System.out.println("   Number of external links  : " + extLinks.size());
            System.out.println("   Number of broken links    : " + brokenLinks.size());
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
    
    public static void main(String[] args) {
        new CrawldaddyApp().runApp(args);
    }
}
