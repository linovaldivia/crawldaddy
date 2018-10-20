package org.lagalag.crawldaddy.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility methods for dealing with various URL string processing needs.
 *
 */
public class URLUtils {
    public static String getPath(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            return path.trim();
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static String getExtension(String urlPath) {
        int extIdx = urlPath.lastIndexOf('.');
        if (extIdx == -1) {
            return null;
        }
        String ext = urlPath.substring(extIdx + 1).trim().toLowerCase();
        return ext;
    }
    
    public static String getHost(String url) {
        if ((url == null) || (url.trim().length() == 0)) {
            return "";
        }
        
        try {
            URL urlObj = new URL(url);
            return urlObj.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }
    
    public static String canonicalize(String inUrl, String baseUrl) {
        if ("/".equals(inUrl.trim())) {
            return baseUrl;
        }
        
        String retUrl = stripAnchor(inUrl);
        return retUrl;
    }
    
    public static String stripAnchor(String url) {
        int anchorIdx = url.indexOf('#');
        if (anchorIdx != -1) {
            url = url.substring(0, anchorIdx);
        }
        return url;
    }
    
    public static String getBaseUrl(String url) {
        try {
            URL u = new URL(url);
            return u.getProtocol() + "://" + u.getHost() + "/";
        } catch (MalformedURLException e) {
        }
        return "";
    }

    private URLUtils() {
        // Prevents instantiation.
    }
}
