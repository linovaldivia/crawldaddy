package org.lagalag.crawldaddy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawldaddyAction extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    
    private String url;
    private CrawldaddyResult result;

    public CrawldaddyAction(String url) {
        this.url = url;
    }
    
    /* Used only when following links from a page that has been downloaded */
    private CrawldaddyAction(String url, String domain, CrawldaddyResult result) {
        this.url = url;
        this.result = result;
    }
    
    public CrawldaddyResult getResult() {
        return this.result;
    }
    
    @Override
    protected void compute() {
        if (url != null) {
            try {
                Document doc = Jsoup.connect(url).get();
                
                if (result == null) {
                    result = new CrawldaddyResult(url);
                }
                
                // Create RecursiveActions to follow links only if they are within the same domain as the input url.
                String inputDomain = getDomain(url);
                System.out.printf("Input url: %s, domain: %s\n", url, inputDomain);
                
                Elements ahrefs = doc.select("a[href]");
                for (Element e : ahrefs) {
                    String link = e.attr("abs:href");
                    
                    if (!result.hasLink(link)) {
                        result.addLink(link);
                        if (getDomain(link).equals(inputDomain)) {
                            // System.out.println("Internal link: " + link);
                        } else {
                            // System.out.println("External link: " + link);
                            result.addExternalLink(link);
                        }
                    }
                }
                Elements scripts = doc.select("script[src]");
                for (Element s : scripts) {
                    result.addExternalScript(s.attr("abs:src"));
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Unacceptable url: " + url);
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.err.println("GET " + url + " resulted in a 404 (Not Found)");
                    if (result != null) {
                        result.addBrokenLink(url);
                    }
                } else {
                    System.err.println("GET " + url + " resulted in a " + e.getStatusCode());
                }
            } catch (IOException e) {
                System.err.println("Unable to GET " + url + ": " + e.getMessage());
            }
        }
    }
    
    private String getDomain(String url) {
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
}
