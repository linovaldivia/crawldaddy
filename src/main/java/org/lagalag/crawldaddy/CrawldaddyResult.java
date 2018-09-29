package org.lagalag.crawldaddy;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CrawldaddyResult {
    private String url;
    private Set<String> allLinks = ConcurrentHashMap.newKeySet();
    // TODO still need CHM or regular HS ok?
    // TODO can we just use allLinks to check for visited status?
    private Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
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
    
    public synchronized boolean checkAndAddLink(String link) {
        if (allLinks.contains(link)) {
            return false;
        }
        allLinks.add(link);
        return true;
    }
    
    public synchronized boolean checkAndAddVisitedLink(String link) {
        if (visitedLinks.contains(link)) {
            return false;
        }
        visitedLinks.add(link);
        return true;
    }
    
    public boolean hasVisitedLink(String link) {
        return visitedLinks.contains(link);
    }
    
    public Set<String> getExternalLinks() {
        return Collections.unmodifiableSet(extLinks);
    }
    
    public void addExternalLink(String link) {
        extLinks.add(link);
    }
    
    public Set<String> getBrokenLinks() {
        return Collections.unmodifiableSet(brokenLinks);
    }
    
    public void addBrokenLink(String brokenLink) {
        brokenLinks.add(brokenLink);
    }
    
    public Set<String> getExternalScripts() {
        return Collections.unmodifiableSet(externalScripts);
    }
    
    public void addExternalScript(String script) {
        externalScripts.add(script);
    }
}
