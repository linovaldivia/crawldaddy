package org.lagalag.crawldaddy;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawldaddyResult {
    private String url;
    private AtomicInteger totalLinkCount = new AtomicInteger(0);
    private Set<String> brokenLinks = ConcurrentHashMap.newKeySet();
    private Set<String> externalScripts = ConcurrentHashMap.newKeySet();
    
    public CrawldaddyResult(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public int getTotalLinkCount() {
        return totalLinkCount.get();
    }
    
    public int incrementTotalLinks() {
        return this.totalLinkCount.incrementAndGet();
    }
    
    public Set<String> getBrokenLinks() {
        return Collections.unmodifiableSet(brokenLinks);
    }
    
    public void addBrokenLink(String brokenLink) {
        this.brokenLinks.add(brokenLink);
    }
    
    public Set<String> getExternalScripts() {
        return Collections.unmodifiableSet(externalScripts);
    }
    
    public void addScript(String script) {
        this.externalScripts.add(script);
    }
}
