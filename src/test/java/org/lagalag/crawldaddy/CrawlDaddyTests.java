package org.lagalag.crawldaddy;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.concurrent.Future;

import org.junit.Test;

public class CrawlDaddyTests {
    @Test
    public void testNullUrl() {
        CrawldaddyResult cdr = doCrawl(null);
        assertNull("Crawling url=null returns non-null result", cdr);
    }
    
    @Test
    public void testBadUrls() {
        CrawldaddyResult cdr = doCrawl("fakeurl");
        assertNull("Crawling url=fakeurl returns non-null result", cdr);
    }

    @Test
    public void test404() {
        CrawldaddyResult cdr = doCrawl("http://example.org/this-no-exist");
        assertNull("Crawling non-existent resource returns non-null result", cdr);
    }
    
    @Test
    public void testCrawlKnownSite1() {
        String urlToTest = "https://www.fgc.cat/";
        CrawldaddyResult cdr = doCrawl(urlToTest);
        assertNotNull("Crawling url=" + urlToTest + " returns null result", cdr);
        
        assertEquals(urlToTest, cdr.getUrl());
        assertNotEquals("No links found in url=" + urlToTest, cdr.getTotalLinkCount(), 0);
        Set<String> brokenLinks = cdr.getBrokenLinks();
        assertNotNull("Set of broken links is null for url=" + urlToTest, brokenLinks);
        Set<String> scripts = cdr.getExternalScripts();
        assertNotNull("Set of scripts is null for url=" + urlToTest, scripts);
        assertNotEquals("No scripts found in url=" + urlToTest, scripts.size(), 0);
    }
    
    private CrawldaddyResult doCrawl(String url) {
        System.out.println("Crawling: " + url);
        Crawldaddy cd = new Crawldaddy(url);
        Future<CrawldaddyResult> cdf = cd.startCrawl();
        assertNotNull("Returned Future is null", cdf);
        try {
            CrawldaddyResult cdr = cdf.get();
            return cdr;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception on Future.get()");
        }
        return null;
    }
}
