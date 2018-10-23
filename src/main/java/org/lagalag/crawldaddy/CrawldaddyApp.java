package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.Set;

import org.lagalag.crawldaddy.pages.PageFetchException;
import org.lagalag.crawldaddy.util.URLUtils;

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
        if (requiredParamsMissing(commandLine)) {
            return null;
        }
        CrawldaddyParams params = new CrawldaddyParams(commandLine.getInputUrl());
        params.setMaxInternalLinks(commandLine.getMaxInternalLinks(CrawldaddyParams.DEFAULT_MAX_INTERNAL_LINKS));
        params.setShowVisitedLink(commandLine.isGenerateVerboseOutputSet());
        params.setNumRepetitions(commandLine.getNumRepetitions(CrawldaddyParams.DEFAULT_CRAWL_REPETITIONS));
        params.setInternalLinksScope(URLUtils.getHost(commandLine.getInputUrl()));
        return params;
    }
    
    private boolean requiredParamsMissing(CrawldaddyCommandLine commandLine) {
        String inputUrl = commandLine.getInputUrl();
        return (inputUrl == null);
    }
    
    private void processResults(CrawldaddyResultSet results, CrawldaddyCommandLine commandLine) {
        CrawldaddyResult resultToDisplay = getResultToDisplay(results);
        processResult(resultToDisplay, commandLine);
        showCrawlTimes(results);
    }
    
    private CrawldaddyResult getResultToDisplay(CrawldaddyResultSet results) {
        CrawldaddyResult resultToDisplay = getFirstSuccessfulResult(results);
        if (resultToDisplay == null) {
            resultToDisplay = results.getFirstResult();
        }
        return resultToDisplay;
    }
    
    private CrawldaddyResult getFirstSuccessfulResult(CrawldaddyResultSet results) {
        return results.stream().filter(result -> result.isHttpStatusOK()).findFirst().orElse(null);
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
    
    private void showCrawlTimes(CrawldaddyResultSet results) {
        if (results.size() == 1) {
            CrawldaddyResult result = results.getFirstResult();
            System.out.println("Total crawl time: " + formatDuration(result.getCrawlTime()));
        } else {
            showIndividualCrawlTimes(results);
            showAverageCrawlTime(results);
        }
    }
    
    private void showIndividualCrawlTimes(CrawldaddyResultSet results) {
        int pass = 1;
        for (CrawldaddyResult result : results) {
            System.out.printf("PASS %01d: %16s\n", pass, formatSummaryOfCrawlResult(result));
            pass++;
        }
    }
    
    private String formatSummaryOfCrawlResult(CrawldaddyResult result) {
        String resultSummary = "";
        if (result.hasPageFetchException()) {
            resultSummary = "EXCEPTION";
        } else if (!result.isHttpStatusOK()) {
            resultSummary = "HTTP " + result.getHttpStatusCode();
        } else {
            resultSummary = formatDuration(result.getCrawlTime());
        }
        return resultSummary;
    }
    
    private void showAverageCrawlTime(CrawldaddyResultSet results) {
        Duration avgCrawlTime = results.getAverageCrawlTime();
        if (!avgCrawlTime.isZero()) {
            System.out.println("Average crawl time: " + formatDuration(avgCrawlTime));
        }
    }
    
    private String formatDuration(Duration duration) {
        StringBuilder ct = new StringBuilder();
        long hours = duration.toHours();
        if (hours > 0) {
            ct.append(hours).append(" hour(s) ");
            duration = duration.minusHours(hours);
        } 
        long mins = duration.toMinutes();
        if (mins > 0) {
            ct.append(mins).append(" minute(s) ");
            duration = duration.minusMinutes(mins);
        } 
        long millis = duration.toMillis();
        long secs =  millis / 1000;
        millis %= 1000;
        if (secs > 0) {
            ct.append(secs).append(".").append(millis).append(" second(s) ");
        }
        return ct.toString();
    }
    
    public static void main(String[] args) {
        new CrawldaddyApp().runApp(args);
    }
}
