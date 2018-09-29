package org.lagalag.crawldaddy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawldaddyAction extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    
    private String url;
    private CrawldaddyResult result;
    private boolean isInitiatingAction;

    public CrawldaddyAction(String url) {
        this.url = url;
        this.isInitiatingAction = true;
    }
    
    /* Used only when following links from a page that has been downloaded */
    private CrawldaddyAction(String url, CrawldaddyResult result) {
        this.url = url;
        this.result = result;
        this.isInitiatingAction = false;
    }
    
    public CrawldaddyResult getResult() {
        return this.result;
    }
    
    @Override
    protected void compute() {
        if (url == null) {
            return;
        }
        
        if (result != null) {
            if (!result.checkAndAddInternalLink(url)) {
                // This link has already been visited by somebody else -- bail out.
                return;
            }
        }

        System.out.println(Thread.currentThread().getName() + ": VISITING: " + url);
        Instant startTime = Instant.now();
        try {
            Document doc = Jsoup.connect(url).get();
            
            if (result == null) {
                result = new CrawldaddyResult(url);
            }
            
            // Create RecursiveActions to follow links only if they are within the same domain as the input url.
            String inputDomain = getDomain(url);
            // System.out.printf("Input url: %s, domain: %s\n", url, inputDomain);
            
            Map<String,CrawldaddyAction> linksToFollow = new HashMap<>();
            Elements ahrefs = doc.select("a[href]");
            for (Element e : ahrefs) {
                String link = canonicalize(e.attr("abs:href"));
                
                // If the link is a self-reference, ignore.
                if (this.url.equalsIgnoreCase(link)) {
                    continue;
                }
                
                // If this link has already been processed (within the same document), ignore.
                if (linksToFollow.containsKey(link)) {
                    continue;
                }
                
                if (getDomain(link).equals(inputDomain)) {
                    // It's an internal link, but have we visited it?
                    if (!result.hasInternalLink(link)) {
                        linksToFollow.put(link, new CrawldaddyAction(link, result));
                    }
                } else {
                    // System.out.println("External link: " + link);
                    result.addExternalLink(link);
                }
            }
            Elements scripts = doc.select("script[src]");
            for (Element s : scripts) {
                result.addExternalScript(s.attr("abs:src"));
            }
            
            if (linksToFollow.size() > 0) {
                invokeAll(linksToFollow.values());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Detected malformed url: " + url);
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                System.err.println("GET " + url + " --> 404 (Not Found)");
                if (result != null) {
                    result.addBrokenLink(url);
                }
            } else {
                System.err.println("GET " + url + " resulted in a " + e.getStatusCode());
            }
        } catch (IOException e) {
            System.err.println("Unable to GET " + url + ": " + e.getMessage());
        } finally {
            if (this.isInitiatingAction && (result != null)) {
                result.setCrawlTime(Duration.between(startTime, Instant.now()));
            }
        }
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
            System.err.println("Detected malformed url: " + url);
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
            URL u = new URL(this.url);
            return u.getProtocol() + "://" + u.getHost() + "/";
        } catch (MalformedURLException e) {
            // Ignored -- shouldn't happen!
        }
        return "";
    }
}
