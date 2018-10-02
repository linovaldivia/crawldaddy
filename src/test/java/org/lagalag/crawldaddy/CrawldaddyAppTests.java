package org.lagalag.crawldaddy;

import static org.junit.Assert.*;

import org.apache.commons.cli.CommandLine;
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
        String[] args = createCommandLineArgs();
        try {
            CommandLine cl = cdApp.parseCommandLine(args);
            assertNull("Non-null CommandLine object returned on empty args", cl);
        } catch (RuntimeException e) {
            fail("Exception on running application with no command line args: " + e.getMessage());
        }
    }
    
    @Test
    public void testSetAllShortOptions() {
        final int maxIntLinks = 1000;
        final boolean useShortOptions = true;
        String[] args = createCommandLineArgs(useShortOptions, 
                                              CrawldaddyApp.CL_OPT_SHOW_EXT_LINKS,
                                              CrawldaddyApp.CL_OPT_SHOW_EXT_SCRIPTS,
                                              CrawldaddyApp.CL_OPT_MAX_INT_LINKS + " " + maxIntLinks);
        try {
            CommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertTrue("Show external links option not set", cl.hasOption(CrawldaddyApp.CL_OPT_SHOW_EXT_LINKS));
            assertTrue("Show external scripts option not set", cl.hasOption(CrawldaddyApp.CL_OPT_SHOW_EXT_SCRIPTS));
            
            CrawldaddyParams params = cdApp.createParams(cl);
            assertNotNull("Params not created from CommandLine object", params);
            assertEquals("Max internal links not set", maxIntLinks, params.getMaxInternalLinks());
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyApp.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }
    
    @Test
    public void testSetAllLongOptions() {
        final int maxIntLinks = 14344;
        final boolean useShortOptions = false;
        String[] args = createCommandLineArgs(useShortOptions,
                                              CrawldaddyApp.CL_LONGOPT_SHOW_EXT_LINKS,
                                              CrawldaddyApp.CL_LONGOPT_SHOW_EXT_SCRIPTS,
                                              CrawldaddyApp.CL_LONGOPT_MAX_INT_LINKS + "=" + maxIntLinks);
        try {
            CommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertTrue("Show external links option not set", cl.hasOption(CrawldaddyApp.CL_OPT_SHOW_EXT_LINKS));
            assertTrue("Show external scripts option not set", cl.hasOption(CrawldaddyApp.CL_OPT_SHOW_EXT_SCRIPTS));
            
            CrawldaddyParams params = cdApp.createParams(cl);
            assertNotNull("Params not created from CommandLine object", params);
            assertEquals("Max internal links not set", maxIntLinks, params.getMaxInternalLinks());
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyApp.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }

    @Test
    public void testFakeArgs() {
        String[] args = createCommandLineArgs("fakearg1", "fakearg2");
        try {
            CommandLine cl = cdApp.parseCommandLine(args);
            assertNull("Non-null CommandLine object returned on empty args", cl);
        } catch (RuntimeException e) {
            fail("Exception on running application with no command line args: " + e.getMessage());
        }
    }
    
    
    @Test
    public void testSetBadMaxInternalLinks1() {
        String[] args = createCommandLineArgs(CrawldaddyApp.CL_OPT_MAX_INT_LINKS + " nonintvalue");
        try {
            CommandLine cl = parseAndAssertNonNullCommandLine(args);
            CrawldaddyParams params = cdApp.createParams(cl);
            assertNotNull("Params not created from CommandLine object", params);
            assertEquals("Max internal links set to non-default value", CrawldaddyParams.DEFAULT_MAX_INTERNAL_LINKS, params.getMaxInternalLinks());
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyApp.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }

    @Test
    public void testSetBadMaxInternalLinks2() {
        String[] args = createCommandLineArgs(CrawldaddyApp.CL_OPT_MAX_INT_LINKS + " -123");
        try {
            CommandLine cl = parseAndAssertNonNullCommandLine(args);
            CrawldaddyParams params = cdApp.createParams(cl);
            assertNotNull("Params not created from CommandLine object", params);
            assertEquals("Max internal links set to non-default value", params.getMaxInternalLinks(), CrawldaddyParams.DEFAULT_MAX_INTERNAL_LINKS);
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyApp.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }

    private String[] createCommandLineArgs(String... args) {
        return createCommandLineArgs(true, args);
    }

    private String[] createCommandLineArgs(boolean useShortOptions, String... args) {
        String[] clArgs = new String[args.length];
        int i = 0;
        String prefix = (useShortOptions ? "-" : "--");
        for (String arg : args) {
            clArgs[i++] = prefix + arg;
        }
        return clArgs;
    }
    
    private CommandLine parseAndAssertNonNullCommandLine(String[] args) {
        CommandLine cl = cdApp.parseCommandLine(args);
        assertNotNull("Args not parsed into CommandLine object", cl);
        return cl;
    }
}
