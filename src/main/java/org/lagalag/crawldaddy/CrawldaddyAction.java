package org.lagalag.crawldaddy;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if ((params == null) || (params.getUrl() == null)) {
            return;
        }
        
        String url = params.getUrl();
        
        if (crawldaddyResult != null) {
            if (!crawldaddyResult.checkAndAddInternalLink(url, params.getMaxInternalLinks())) {
                // This link has already been visited or we've hit the internal links limit -- bail out.
                return;
            }
        }

        if (params.getShowVisitedLink()) {
            System.out.println("VISITING: " + url);
        }
        LOGGER.debug("VISITING: " + url);
        crawlLink(url);
    }
    
    private void crawlLink(String url) {
        Instant startTime = (this.isInitiatingAction ? Instant.now() : null);
        try {
            PageFetchService pageFetchService = PageFetchServiceLocator.getService();
            pageFetchService.fetch(url, this);
        } catch (PageFetchException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (this.isInitiatingAction && (crawldaddyResult != null)) {
                crawldaddyResult.setCrawlTime(Duration.between(startTime, Instant.now()));
            }
        }
    }
    
    @Override
    public void handlePageFetchResults(PageFetchResults pageFetchResults) {
        if (pageFetchResults.isHttpStatusOK()) {
            // TODO consider returning a Special Case object instead of null result
            if (crawldaddyResult == null) {
                crawldaddyResult = new CrawldaddyResult(params.getUrl());
            }
            Collection<CrawldaddyAction> urlsToFollow = processLinkUrls(pageFetchResults.getLinkUrls());
            processScriptUrls(pageFetchResults.getScriptUrls());
            if (urlsToFollow.size() > 0) {
                invokeAll(urlsToFollow);
            }
        } else {
            handleNonOKHttpStatus(pageFetchResults.getUrl(), pageFetchResults.getHttpStatusCode());
        }
    }
    
    private Collection<CrawldaddyAction> processLinkUrls(List<String> linkUrls) {
        Map<String,CrawldaddyAction> urlsToFollow = new HashMap<>();
        String myUrl = params.getUrl();
        String baseUrl = URLUtils.getBaseUrl(myUrl);
        for (String linkUrl : linkUrls) {
            linkUrl = URLUtils.canonicalize(linkUrl, baseUrl);
            // Some sites have empty hrefs apparently.
            if (linkUrl.trim().length() == 0) {
                continue;
            }
            
            // If the link is a self-reference, ignore.
            if (myUrl.equalsIgnoreCase(linkUrl)) {
                continue;
            }
            
            // If this link was already seen (within the same document), ignore.
            if (urlsToFollow.containsKey(linkUrl)) {
                continue;
            }

            CrawldaddyAction actionForUrlToFollow = triageLinkUrl(linkUrl);
            if (actionForUrlToFollow != null) {
                urlsToFollow.put(linkUrl, actionForUrlToFollow);
            }
        }
        return urlsToFollow.values();
    }
    
    private CrawldaddyAction triageLinkUrl(String linkUrl) {
        CrawldaddyAction actionForUrlToFollow = null;
        if (isSupportedType(linkUrl)) {
            if (isInternalLink(linkUrl)) {
                if (!hasInternalLinkBeenVisited(linkUrl)) {
                    CrawldaddyParams newParams = new CrawldaddyParams(linkUrl, params);
                    actionForUrlToFollow = new CrawldaddyAction(newParams, crawldaddyResult);
                }
            } else {
                crawldaddyResult.addExternalLink(linkUrl);
            }
        }
        return actionForUrlToFollow;
    }
    
    private boolean isInternalLink(String url) {
        return hasSameHost(url, internalLinksScope);
    }
    
    private boolean hasInternalLinkBeenVisited(String url) {
        return crawldaddyResult.hasInternalLink(url);
    }
    
    private void handleNonOKHttpStatus(String url, int httpStatusCode) {
        if (httpStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOGGER.error("GET " + url + " --> 404 (Not Found)");
            if (crawldaddyResult != null) {
                crawldaddyResult.addBrokenLink(url);
            }
        } else {
            LOGGER.error("GET " + url + " resulted in a " + httpStatusCode);
        }
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
