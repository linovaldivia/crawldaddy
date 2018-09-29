package org.lagalag.crawldaddy;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.concurrent.Future;

import org.junit.Test;

public class CrawldaddyTests {
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
        CrawldaddyResult cdr = doCrawl("http://example.org/this-is-a-404");
        assertNull("Crawling non-existent resource returns non-null result", cdr);
    }
    
    @Test
    public void testCrawlKnownSite1() {
        String urlToTest = "https://www.mizcracker.com";
        CrawldaddyResult cdr = doCrawl(urlToTest);
        assertNotNull("Crawling url=" + urlToTest + " returns null result", cdr);
        
        assertEquals(urlToTest, cdr.getUrl());
        Set<String> intLinks = cdr.getInternalLinks();
        assertNotNull("Set of internal links is null for url=" + urlToTest, intLinks);
        assertTrue("No internal links found", intLinks.size() > 0);
        assertTrue("Num internal links > Num all links", intLinks.size() <= cdr.getTotalLinkCount());
        Set<String> extLinks = cdr.getExternalLinks();
        assertNotNull("Set of external links is null for url=" + urlToTest, extLinks);
        assertTrue("No external links found", extLinks.size() > 0);
        assertTrue("Num external links > Num all links", extLinks.size() <= cdr.getTotalLinkCount());
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
