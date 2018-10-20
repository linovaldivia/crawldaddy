package org.lagalag.crawldaddy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Retrieves and processes the document at a specified URL, extracting links and external javascript references.
 * Internal links (e.g. links that are in the same domain as the initial URL) will also be retrieved and processed, 
 * possibly in another thread.
 */
public class CrawldaddyAction extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final List<String> UNSUPPORTED_TYPES = Arrays.asList("jpg", "pdf", "png", "gif");
    
    private CrawldaddyParams params;
    private CrawldaddyResult result;
    private String internalLinksScope;
    private boolean isInitiatingAction;

    public CrawldaddyAction(CrawldaddyParams params) {
        this.params = params;
        this.isInitiatingAction = true;
        this.internalLinksScope = getHost(params.getUrl());
    }
    
    /* Used only when following links from a page that has been downloaded */
    private CrawldaddyAction(CrawldaddyParams params, CrawldaddyResult result) {
        this.params = params;
        this.result = result;
        this.isInitiatingAction = false;
        this.internalLinksScope = getHost(params.getUrl());
    }
    
    public CrawldaddyResult getResult() {
        return this.result;
    }
    
    @Override
    protected void compute() {
        if ((params == null) || (params.getUrl() == null)) {
            return;
        }
        
        String url = params.getUrl();
        
        if (result != null) {
            if (!result.checkAndAddInternalLink(url, params.getMaxInternalLinks())) {
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
            Document doc = Jsoup.connect(url).get();
            
            if (result == null) {
                result = new CrawldaddyResult(params.getUrl());
            }
            
            Elements linkElements = doc.select("a[href]");
            Collection<CrawldaddyAction> urlsToFollow = extractLinks(linkElements);
            extractExternalScripts(doc.select("script[src]"));
            
            if (urlsToFollow.size() > 0) {
                invokeAll(urlsToFollow);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Detected malformed url: " + url);
        } catch (HttpStatusException e) {
            handleHttpStatusException(url, e);
        } catch (IOException e) {
            LOGGER.error("Unable to GET " + url + ": " + e.getMessage());
        } finally {
            if (this.isInitiatingAction && (result != null)) {
                result.setCrawlTime(Duration.between(startTime, Instant.now()));
            }
        }
    }
    
    private Collection<CrawldaddyAction> extractLinks(Elements elems) {
        Map<String,CrawldaddyAction> urlsToFollow = new HashMap<>();
        for (Element e : elems) {
            String url = canonicalize(e.attr("abs:href"));
            // Some sites have empty hrefs apparently.
            if (url.trim().length() == 0) {
                continue;
            }
            
            // If the link is a self-reference, ignore.
            if (params.getUrl().equalsIgnoreCase(url)) {
                continue;
            }
            
            // If this link was already seen (within the same document), ignore.
            if (urlsToFollow.containsKey(url)) {
                continue;
            }
            
            if (isSupportedType(url)) {
                if (isInternalLink(url)) {
                    if (!hasInternalLinkBeenVisited(url)) {
                        CrawldaddyParams newParams = new CrawldaddyParams(url, params);
                        urlsToFollow.put(url, new CrawldaddyAction(newParams, result));
                    }
                } else {
                    result.addExternalLink(url);
                }
            }
        }
        return urlsToFollow.values();
    }
    
    private boolean isSupportedType(String url) {
        String path = getPath(url);
        if (path == null) {
            // Possibly malformed URL -- definitely not supported.
            return false;
        }
        if (path.isEmpty()) {
            // URL has no path -- assume it's an HTML doc.
            return true;
        }
        String ext = getExtension(path);
        if (ext == null) {
            // No extension -- assume it's an HTML doc.
            return true;
        }
        return (!UNSUPPORTED_TYPES.contains(ext));
    }
    
    private String getPath(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            return path.trim();
        } catch (MalformedURLException e) {
            LOGGER.error("Detected malformed url: " + url);
            return null;
        }
    }
    
    private String getExtension(String urlPath) {
        int extIdx = urlPath.lastIndexOf('.');
        if (extIdx == -1) {
            return null;
        }
        String ext = urlPath.substring(extIdx + 1).trim().toLowerCase();
        return ext;
    }
    
    private boolean isInternalLink(String url) {
        return hasSameHost(url, internalLinksScope);
    }
    
    private boolean hasInternalLinkBeenVisited(String url) {
        return result.hasInternalLink(url);
    }
    
    private void handleHttpStatusException(String url, HttpStatusException e) {
        if (e.getStatusCode() == 404) {
            LOGGER.error("GET " + url + " --> 404 (Not Found)");
            if (result != null) {
                result.addBrokenLink(url);
            }
        } else {
            LOGGER.error("GET " + url + " resulted in a " + e.getStatusCode());
        }
    }
    
    private void extractExternalScripts(Elements scripts) {
        for (Element s : scripts) {
            result.addExternalScript(s.attr("abs:src"));
        }
    }
    
    private boolean hasSameHost(String url, String host) {
        return getHost(url).equals(host);
    }
    
    private String getHost(String url) {
        if ((url == null) || (url.trim().length() == 0)) {
            return "";
        }
        if ("/".equals(url.trim())) {
            url = getBaseUrl();
        }
        
        try {
            URL urlObj = new URL(url);
            return urlObj.getHost();
        } catch (MalformedURLException e) {
            LOGGER.error("Detected malformed url: " + url);
            return "";
        }
    }
    
    private String canonicalize(String inUrl) {
        if ("/".equals(inUrl.trim())) {
            return getBaseUrl();
        }
        
        String retUrl = stripAnchor(inUrl);
        return retUrl;
    }
    
    private String stripAnchor(String url) {
        int anchorIdx = url.indexOf('#');
        if (anchorIdx != -1) {
            url = url.substring(0, anchorIdx);
        }
        return url;
    }
    
    private String getBaseUrl() {
        try {
            URL u = new URL(params.getUrl());
            return u.getProtocol() + "://" + u.getHost() + "/";
        } catch (MalformedURLException e) {
            // Ignored -- shouldn't happen!
        }
        return "";
    }
}
