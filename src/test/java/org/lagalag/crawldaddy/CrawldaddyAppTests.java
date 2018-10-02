package org.lagalag.crawldaddy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

/**
 * Junit tests for command line args and app output.
 */
public class CrawldaddyAppTests {
    private CrawldaddyApp cdApp;
    
    @Before
    public void beforeTest() {
        cdApp = new CrawldaddyApp();
    }
    
    @Test
    public void testNoArgs() {
        String[] args = {};
        try {
            int exitCode = cdApp.runApp(args);
            assertTrue("Application exited with success exit code", (exitCode != 0));
        } catch (RuntimeException e) {
            fail("Exception on running application with no command line args: " + e.getMessage());
        }
    }
    
    @Test
    public void testSetBadMaxInternalLinks() {
        String[] args = { "-" + CrawldaddyApp.CL_OPT_MAX_INT_LINKS + " abcde" };
        try {
            cdApp.runApp(args);
            // TODO check that max int links uses default
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyApp.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }
    
    // TODO add check to set max int links to a valid value
}
