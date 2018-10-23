package org.lagalag.crawldaddy;

import org.lagalag.crawldaddy.util.URLUtils;

/**
 * Class that encapsulates parameters used to control the crawler internals.
 *
 */
public class CrawldaddyParams {
    public static final int DEFAULT_MAX_INTERNAL_LINKS = 3000;
    public static final boolean DEFAULT_SHOW_VISITED_LINK = true;
    public static final int DEFAULT_CRAWL_REPETITIONS = 1;
    public static final int MAX_CRAWL_REPETITIONS = 10;
    
    private String url;
    private String internalLinksScope;
    private int maxInternalLinks = DEFAULT_MAX_INTERNAL_LINKS;
    private boolean showVisitedLink = DEFAULT_SHOW_VISITED_LINK;
    private int numRepetitions = DEFAULT_CRAWL_REPETITIONS;
    
    public CrawldaddyParams(String url) {
        this.url = url;
        // By default, only links to resources in the same host are considered "internal links".
        internalLinksScope = URLUtils.getHost(url);
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getInternalLinksScope() {
        return internalLinksScope;
    }

    public void setInternalLinksScope(String internalLinksScope) {
        this.internalLinksScope = internalLinksScope;
    }

    public int getMaxInternalLinks() {
        return maxInternalLinks;
    }
    
    public void setMaxInternalLinks(int maxInternalLinks) {
        this.maxInternalLinks = maxInternalLinks;
    }
    
    public boolean getShowVisitedLink() {
        return showVisitedLink;
    }
    
    public void setShowVisitedLink(boolean showVisitedLink) {
        this.showVisitedLink = showVisitedLink;
    }
    
    public int getNumRepetitions() {
        return numRepetitions;
    }
    
    public void setNumRepetitions(int numRepetitions) {
        if ((numRepetitions <= MAX_CRAWL_REPETITIONS) && (numRepetitions > 0)) {
            this.numRepetitions = numRepetitions;
        }
    }
}