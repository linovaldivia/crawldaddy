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
    
    private static final List<String> UNSUPPORTED_TYPES = Arrays.asList("jpg", "pdf", "png", "gif");
    
    private CrawldaddyParams params;
    private CrawldaddyResult crawldaddyResult;
    private String internalLinksScope;
    private boolean isInitiatingAction;

    public CrawldaddyAction(CrawldaddyParams params) {
        this.params = params;
        this.isInitiatingAction = true;
        this.internalLinksScope = URLUtils.getHost(params.getUrl());
        this.crawldaddyResult = new CrawldaddyResult(params.getUrl());
    }
    
    /* Used only when following links from a page that has been downloaded */
    private CrawldaddyAction(CrawldaddyParams params, CrawldaddyResult result) {
        this.params = params;
        this.crawldaddyResult = result;
        this.isInitiatingAction = false;
        this.internalLinksScope = URLUtils.getHost(params.getUrl());
    }
    
    public CrawldaddyResult getResult() {
        return this.crawldaddyResult;
    }
    
    @Override
    protected void compute() {
        String url = params.getUrl();
        if (params.getShowVisitedLink()) {
            System.out.println("VISITING: " + url);
        }
        LOGGER.debug("VISITING: " + url);
        fetchPageAtUrl(url);
    }
    
    private void fetchPageAtUrl(String url) {
        Instant startTime = (isInitiatingAction ? Instant.now() : null);
        try {
            PageFetchService pageFetchService = PageFetchServiceLocator.getService();
            pageFetchService.fetch(url, this);
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
        String myUrl = params.getUrl();
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
        String myUrl = params.getUrl();
        if (myUrl.equalsIgnoreCase(linkUrl)) {
            skipProcessing = true;
        }
        // If the link is a reference to the initial url, skip it. 
        String initialUrl = params.getInitialUrl();
        if (initialUrl.equalsIgnoreCase(linkUrl)) {
            skipProcessing = true;
        }
        if (!isSupportedType(linkUrl)) {
            skipProcessing = true;
        }
        return skipProcessing;
    }
    
    private boolean isInternalLink(String url) {
        return hasSameHost(url, internalLinksScope);
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
            childCrawlerForLinkUrl = new CrawldaddyAction(new CrawldaddyParams(linkUrl, params), crawldaddyResult);
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
