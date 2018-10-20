package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Holds the results of the web crawl.
 */
public class CrawldaddyResult {
    private String url;
    private ConcurrentMap<String,Boolean> intLinks = new ConcurrentHashMap<>();
    private Set<String> extLinks = ConcurrentHashMap.newKeySet();
    private Set<String> brokenLinks = ConcurrentHashMap.newKeySet();
    private Set<String> externalScripts = ConcurrentHashMap.newKeySet();
    private Duration crawlTime;
    
    public CrawldaddyResult(String url) {
        this.url = url;
    }
    
    public void setCrawlTime(Duration crawlTime) {
        this.crawlTime = crawlTime;
    }
    
    public Duration getCrawlTime() {
        return this.crawlTime;
    }
    
    public String getUrl() {
        return url;
    }
    
    public int getTotalLinkCount() {
        return getInternalLinkCount() + getExternalLinkCount() + getBrokenLinkCount();
    }
    
    public Set<String> getInternalLinks() {
        return Collections.unmodifiableSet(intLinks.keySet());
    }
    
    public boolean checkAndAddInternalLink(String url, int maxInternalLinks) {
        synchronized(intLinks) {
            if (intLinks.size() < maxInternalLinks) {
                // Return true only if the given link is not already in the map 
                return (intLinks.putIfAbsent(url, Boolean.TRUE) == null);
            }
        }
        return false;
    }
    
    public boolean hasInternalLink(String url) {
        return intLinks.containsKey(url);
    }
    
    public int getInternalLinkCount() {
        return intLinks.size();
    }
    
    public Set<String> getExternalLinks() {
        return Collections.unmodifiableSet(extLinks);
    }
    
    public void addExternalLink(String url) {
        extLinks.add(url);
    }
    
    public int getExternalLinkCount() {
        return extLinks.size();
    }
    
    public Set<String> getBrokenLinks() {
        return Collections.unmodifiableSet(brokenLinks);
    }
    
    public void addBrokenLink(String brokenLink) {
        brokenLinks.add(brokenLink);
    }
    
    public int getBrokenLinkCount() {
        return brokenLinks.size();
    }
    
    public Set<String> getExternalScripts() {
        return Collections.unmodifiableSet(externalScripts);
    }
    
    public void addExternalScript(String script) {
        externalScripts.add(script);
    }
    
    public int getExternalScriptsCount() {
        return externalScripts.size();
    }
}
