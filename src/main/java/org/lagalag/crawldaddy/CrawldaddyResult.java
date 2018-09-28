package org.lagalag.crawldaddy;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CrawldaddyResult {
    private String url;
    private Set<String> allLinks = ConcurrentHashMap.newKeySet();
    private Set<String> extLinks = ConcurrentHashMap.newKeySet();
    private Set<String> brokenLinks = ConcurrentHashMap.newKeySet();
    private Set<String> externalScripts = ConcurrentHashMap.newKeySet();
    
    public CrawldaddyResult(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Set<String> getAllLinks() {
        return Collections.unmodifiableSet(allLinks);
    }
    
    public void addLink(String link) {
        this.allLinks.add(link);
    }
    
    public boolean hasLink(String link) {
        return this.allLinks.contains(link);
    }
    
    public Set<String> getExternalLinks() {
        return Collections.unmodifiableSet(extLinks);
    }
    
    public void addExternalLink(String link) {
        this.extLinks.add(link);
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
    
    public void addExternalScript(String script) {
        this.externalScripts.add(script);
    }
}
