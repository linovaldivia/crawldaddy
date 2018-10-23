package org.lagalag.crawldaddy;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RecursiveAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lagalag.crawldaddy.pages.PageFetchConsumer;
import org.lagalag.crawldaddy.pages.PageFetchException;
import org.lagalag.crawldaddy.pages.PageFetchResults;
import org.lagalag.crawldaddy.pages.PageFetchService;
import org.lagalag.crawldaddy.pages.PageFetchServiceLocator;
import org.lagalag.crawldaddy.util.URLUtils;

/**
 * Retrieves and processes the document at a specified URL, extracting links and external javascript references.
 * Internal links (e.g. links that are in the same domain as the initial URL) will also be retrieved and processed, 
 * possibly in another thread.
 */
public class CrawldaddyAction extends RecursiveAction implements PageFetchConsumer {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final List<String> UNSUPPORTED_TYPES = Arrays.asList("jpg", "pdf", "png", "gif", "zip", "tar", "jar", "gz");
    
    private String myUrl;
    private CrawldaddyParams params;
    private CrawldaddyResult crawldaddyResult;
    private boolean isInitiatingAction;

    public CrawldaddyAction(CrawldaddyParams params) {
        this.myUrl = params.getUrl();
        this.params = params;
        this.crawldaddyResult = new CrawldaddyResult(myUrl);
        this.isInitiatingAction = true;
    }
    
    /* Used only when crawling links from a page that has been downloaded */
    private CrawldaddyAction(String url, CrawldaddyParams params, CrawldaddyResult result) {
        this.myUrl = url;
        this.params = params;
        this.crawldaddyResult = result;
        this.isInitiatingAction = false;
    }
    
    public CrawldaddyResult getResult() {
        return this.crawldaddyResult;
    }
    
    @Override
    protected void compute() {
        if (params.getShowVisitedLink()) {
            System.out.println("VISITING: " + myUrl);
        }
        LOGGER.debug("VISITING: " + myUrl);
        fetchPageAtMyUrl();
    }
    
    private void fetchPageAtMyUrl() {
        Instant startTime = (isInitiatingAction ? Instant.now() : null);
        try {
            PageFetchService pageFetchService = PageFetchServiceLocator.getService();
            pageFetchService.fetch(myUrl, this);
        } catch (PageFetchException e) {
            LOGGER.error(e.getMessage());
            if (isInitiatingAction) {
                crawldaddyResult.setPageFetchException(e);
            }
        } finally {
            if (isInitiatingAction) {
                crawldaddyResult.setCrawlTime(Duration.between(startTime, Instant.now()));
            }
        }
    }
    
    @Override
    public void handlePageFetchResults(PageFetchResults pageFetchResults) {
        if (isInitiatingAction) {
            crawldaddyResult.setHttpStatusCode(pageFetchResults.getHttpStatusCode());
        }
        if (pageFetchResults.isHttpStatusOK()) {
            processScriptUrls(pageFetchResults.getScriptUrls());
            
            Collection<CrawldaddyAction> childCrawlers = processLinkUrls(pageFetchResults.getLinkUrls());
            if (childCrawlers.size() > 0) {
                invokeAll(childCrawlers);
            }
        } else {
            handleNonOKHttpStatus(pageFetchResults.getUrl(), pageFetchResults.getHttpStatusCode());
        }
    }
    
    private Collection<CrawldaddyAction> processLinkUrls(List<String> linkUrls) {
        Collection<CrawldaddyAction> childCrawlers = new ArrayList<>();
        String baseUrl = URLUtils.getBaseUrl(myUrl);
        for (String linkUrl : linkUrls) {
            linkUrl = URLUtils.canonicalize(linkUrl, baseUrl);
            if (skipProcessing(linkUrl)) {
                continue;
            }
            if (isInternalLink(linkUrl)) {
                processInternalLinkUrl(linkUrl).ifPresent(childCrawler -> childCrawlers.add(childCrawler));
            } else {
                processExternalLinkUrl(linkUrl);
            }
        }
        return childCrawlers;
    }
    
    private boolean skipProcessing(String linkUrl) {
        boolean skipProcessing = false;
        // Some sites have empty hrefs apparently.
        if (linkUrl.trim().length() == 0) {
            skipProcessing = true;
        }
        // If the link is a self-reference, skip it.
        if (myUrl.equalsIgnoreCase(linkUrl)) {
            skipProcessing = true;
        }
        // If the link points back to the initial url, skip it. 
        String initialUrl = params.getUrl();
        if (initialUrl.equalsIgnoreCase(linkUrl)) {
            skipProcessing = true;
        }
        if (!isSupportedType(linkUrl)) {
            skipProcessing = true;
        }
        return skipProcessing;
    }
    
    private boolean isInternalLink(String url) {
        return hasSameHost(url, params.getInternalLinksScope());
    }
    
    private void handleNonOKHttpStatus(String url, int httpStatusCode) {
        if (httpStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOGGER.error("GET " + url + " --> 404 (Not Found)");
            crawldaddyResult.addBrokenLink(url);
        } else {
            LOGGER.error("GET " + url + " resulted in a " + httpStatusCode);
        }
    }

    private Optional<CrawldaddyAction> processInternalLinkUrl(String linkUrl) {
        CrawldaddyAction childCrawlerForLinkUrl = null;
        // Create a child crawler iff we haven't visited/planned to visit this url.
        if (crawldaddyResult.checkAndAddInternalLink(linkUrl, params.getMaxInternalLinks())) {
            childCrawlerForLinkUrl = new CrawldaddyAction(linkUrl, params, crawldaddyResult);
        }
        return Optional.ofNullable(childCrawlerForLinkUrl);
    }
    
    private void processExternalLinkUrl(String linkUrl) {
        crawldaddyResult.addExternalLink(linkUrl);
    }

    private void processScriptUrls(List<String> scriptUrls) {
        crawldaddyResult.addExternalScripts(scriptUrls);
    }
    
    private boolean hasSameHost(String url, String inputHost) {
        String urlHost = URLUtils.getHost(url);
        return urlHost.equals(inputHost);
    }
    
    private boolean isSupportedType(String url) {
        String path = URLUtils.getPath(url);
        if (path == null) {
            // Possibly malformed URL -- definitely not supported.
            return false;
        }
        if (path.isEmpty()) {
            // URL has no path -- assume it's an HTML doc.
            return true;
        }
        String ext = URLUtils.getExtension(path);
        if (ext == null) {
            // No extension -- assume it's an HTML doc.
            return true;
        }
        return (!UNSUPPORTED_TYPES.contains(ext));
    }
}
