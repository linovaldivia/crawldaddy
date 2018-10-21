package org.lagalag.crawldaddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

/**
 * Junit tests for command line args.
 */
public class CrawldaddyCommandLineTests {
    @Test
    public void testHelpOutput() {
        StringWriter sw = new StringWriter();
        CrawldaddyCommandLine.showHelp(new PrintWriter(sw));
        String help = sw.toString();
        assertNotNull("No help (usage) output shown!", help);
        assertTrue("Help (usage) message doesn't contain app name", help.contains("crawldaddy"));
    }
    
    @Test
    public void testNoArgs() {
        String[] args = createCommandLineArgs();
        try {
            CrawldaddyCommandLine cl = CrawldaddyCommandLine.parse(args);
            assertNull("Non-null CommandLine object returned on empty args", cl);
        } catch (RuntimeException e) {
            fail("Exception on running application with no command line args: " + e.getMessage());
        }
    }
    
    @Test
    public void testSetAllShortOptions() {
        final int maxIntLinks = 1000;
        final int numRepetitions = 7;
        final boolean useShortOptions = true;
        String[] args = createCommandLineArgs(useShortOptions, 
                                              CrawldaddyCommandLine.CL_OPT_SHOW_EXT_LINKS,
                                              CrawldaddyCommandLine.CL_OPT_SHOW_EXT_SCRIPTS,
                                              CrawldaddyCommandLine.CL_OPT_GENERATE_VERBOSE_OUTPUT,
                                              CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + " " + maxIntLinks,
                                              CrawldaddyCommandLine.CL_LONGOPT_CRAWL_REPEATEDLY + " " + numRepetitions);
        try {
            CrawldaddyCommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertTrue("Show external links option not set", cl.isShowExternalLinksSet());
            assertTrue("Show external scripts option not set", cl.isShowExternalScriptsSet());
            assertTrue("Generate verbose output option not set", cl.isGenerateVerboseOutputSet());
            assertEquals("Max internal links not set", maxIntLinks, cl.getMaxInternalLinks(0));
            assertEquals("Num repetitions not set", numRepetitions, cl.getNumRepetitions(0));
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }
    
    @Test
    public void testSetAllLongOptions() {
        final int maxIntLinks = 14344;
        final int numRepetitions = 8;
        final boolean useShortOptions = false;
        String[] args = createCommandLineArgs(useShortOptions,
                                              CrawldaddyCommandLine.CL_LONGOPT_SHOW_EXT_LINKS,
                                              CrawldaddyCommandLine.CL_LONGOPT_SHOW_EXT_SCRIPTS,
                                              CrawldaddyCommandLine.CL_LONGOPT_MAX_INT_LINKS + "=" + maxIntLinks,
                                              CrawldaddyCommandLine.CL_LONGOPT_CRAWL_REPEATEDLY + "=" + numRepetitions);
        try {
            CrawldaddyCommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertTrue("Show external links option not set", cl.isShowExternalLinksSet());
            assertTrue("Show external scripts option not set", cl.isShowExternalScriptsSet());
            assertEquals("Max internal links not set", maxIntLinks, cl.getMaxInternalLinks(0));
            assertEquals("Num repetitions not set", numRepetitions, cl.getNumRepetitions(0));
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }

    @Test
    public void testFakeArgs() {
        String[] args = createCommandLineArgs("fakearg1", "fakearg2");
        try {
            CrawldaddyCommandLine cl = CrawldaddyCommandLine.parse(args);
            assertNull("Non-null CrawldaddyCommandLine object returned on empty args", cl);
        } catch (RuntimeException e) {
            fail("Exception on running application with no command line args: " + e.getMessage());
        }
    }
    
    @Test
    public void testSetBadMaxInternalLinks1() {
        int expectedDefault = 10;
        String[] args = createCommandLineArgs(CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + " nonintvalue");
        try {
            CrawldaddyCommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertEquals("Max internal links set to unexpected value", expectedDefault, cl.getMaxInternalLinks(expectedDefault));
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }

    @Test
    public void testSetBadMaxInternalLinks2() {
        int expectedDefault = 5678;
        String[] args = createCommandLineArgs(CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + " -123");
        try {
            CrawldaddyCommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertEquals("Max internal links set to unexpected value", expectedDefault, cl.getMaxInternalLinks(expectedDefault));
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyCommandLine.CL_OPT_MAX_INT_LINKS + ": " + e.getMessage());
        }
    }

    @Test
    public void testSetBadNumRepetitions1() {
        int expectedDefault = 10;
        String[] args = createCommandLineArgs(CrawldaddyCommandLine.CL_OPT_CRAWL_REPEATEDLY + " nonintvalue");
        try {
            CrawldaddyCommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertEquals("Num repetitions set to unexpected value", expectedDefault, cl.getNumRepetitions(expectedDefault));
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyCommandLine.CL_OPT_CRAWL_REPEATEDLY + ": " + e.getMessage());
        }
    }
    
    @Test
    public void testSetBadNumRepetitions2() {
        int expectedDefault = 5;
        String[] args = createCommandLineArgs(CrawldaddyCommandLine.CL_OPT_CRAWL_REPEATEDLY + " -42");
        try {
            CrawldaddyCommandLine cl = parseAndAssertNonNullCommandLine(args);
            assertEquals("Num repetitions set to unexpected value", expectedDefault, cl.getNumRepetitions(expectedDefault));
        } catch (RuntimeException e) {
            fail("Exception on running application with illegal value for option " + CrawldaddyCommandLine.CL_OPT_CRAWL_REPEATEDLY + ": " + e.getMessage());
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
    
    private CrawldaddyCommandLine parseAndAssertNonNullCommandLine(String[] args) {
        CrawldaddyCommandLine cl = CrawldaddyCommandLine.parse(args);
        assertNotNull("Args not parsed into CrawldaddyCommandLine object", cl);
        return cl;
    }
}
