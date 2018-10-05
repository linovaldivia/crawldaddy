package org.lagalag.crawldaddy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
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
    
    private CrawldaddyParams params;
    private CrawldaddyResult result;
    private boolean isInitiatingAction;

    public CrawldaddyAction(CrawldaddyParams params) {
        this.params = params;
        this.isInitiatingAction = true;
    }
    
    /* Used only when following links from a page that has been downloaded */
    private CrawldaddyAction(CrawldaddyParams params, CrawldaddyResult result) {
        this.params = params;
        this.result = result;
        this.isInitiatingAction = false;
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
        Instant startTime = (this.isInitiatingAction ? Instant.now() : null);
        try {
            Document doc = Jsoup.connect(url).get();
            
            if (result == null) {
                result = new CrawldaddyResult(params.getUrl());
            }
            
            Collection<CrawldaddyAction> linksToFollow = extractLinks(doc.select("a[href]"), getDomain(url));
            extractExternalScripts(doc.select("script[src]"));
            
            if (linksToFollow.size() > 0) {
                invokeAll(linksToFollow);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Detected malformed url: " + url);
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                LOGGER.error("GET " + url + " --> 404 (Not Found)");
                if (result != null) {
                    result.addBrokenLink(url);
                }
            } else {
                LOGGER.error("GET " + url + " resulted in a " + e.getStatusCode());
            }
        } catch (IOException e) {
            LOGGER.error("Unable to GET " + url + ": " + e.getMessage());
        } finally {
            if (this.isInitiatingAction && (result != null)) {
                result.setCrawlTime(Duration.between(startTime, Instant.now()));
            }
        }
    }
    
    private Collection<CrawldaddyAction> extractLinks(Elements elems, String inputDomain) {
        Map<String,CrawldaddyAction> linksToFollow = new HashMap<>();
        for (Element e : elems) {
            String link = canonicalize(e.attr("abs:href"));
            // Some sites have empty hrefs apparently.
            if (link.trim().length() == 0) {
                continue;
            }
            
            // If the link is a self-reference, ignore.
            if (params.getUrl().equalsIgnoreCase(link)) {
                continue;
            }
            
            // If this link has already been processed (within the same document), ignore.
            if (linksToFollow.containsKey(link)) {
                continue;
            }
            
            if (isInDomain(link, inputDomain)) {
                // It's an internal link, but have we already visited it?
                if (!result.hasInternalLink(link)) {
                    CrawldaddyParams newParams = new CrawldaddyParams(link, params);
                    linksToFollow.put(link, new CrawldaddyAction(newParams, result));
                }
            } else {
                result.addExternalLink(link);
            }
        }
        return linksToFollow.values();
    }
    
    private void extractExternalScripts(Elements scripts) {
        for (Element s : scripts) {
            result.addExternalScript(s.attr("abs:src"));
        }
    }
    
    private boolean isInDomain(String link, String inputDomain) {
        return getDomain(link).equals(inputDomain);
    }
    
    private String getDomain(String url) {
        if (url.trim().length() == 0) {
            return "";
        }
        if ("/".equals(url.trim())) {
            url = getBaseUrl();
        }
        
        try {
            URL urlObj = new URL(url);
            String hostname = urlObj.getHost();
            String[] parts = hostname.split("\\.");
            int numParts = parts.length;
            if (numParts >= 2) {
                // Use only the last two parts of the hostname.
                return parts[numParts - 2] + "." + parts[numParts - 1];
            } else {
                // Use the whole hostname parsed from input url.
                return hostname;
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Detected malformed url: " + url);
            return "";
        }
    }
    
    private String canonicalize(String inUrl) {
        if ("/".equals(inUrl.trim())) {
            return getBaseUrl();
        }
        
        String retUrl = inUrl;
        // Strip anchor.
        int anchorIdx = retUrl.indexOf('#');
        if (anchorIdx != -1) {
            retUrl = retUrl.substring(0, anchorIdx);
        }
        
        return retUrl;
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
