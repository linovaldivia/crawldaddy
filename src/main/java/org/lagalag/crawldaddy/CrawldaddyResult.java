package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    
    public boolean checkAndAddInternalLink(String link) {
        // Return true only if the given link is not already in the map 
        // (i.e. previous value before putIfAbsent(link) call is null).
        // ConcurrentMap gives us the guarantee that this "check-and-set" operation will be atomic.
        return (intLinks.putIfAbsent(link, Boolean.TRUE) == null);
    }
    
    public boolean hasInternalLink(String link) {
        return intLinks.containsKey(link);
    }
    
    public int getInternalLinkCount() {
        return intLinks.size();
    }
    
    public Set<String> getExternalLinks() {
        return Collections.unmodifiableSet(extLinks);
    }
    
    public void addExternalLink(String link) {
        extLinks.add(link);
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
