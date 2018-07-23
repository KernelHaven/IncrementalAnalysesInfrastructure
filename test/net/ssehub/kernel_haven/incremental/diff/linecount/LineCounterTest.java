package net.ssehub.kernel_haven.incremental.diff.linecount;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;


/**
 * The Class LineCounterTest.
 * @author moritz
 */
public class LineCounterTest {

    /**
     * Tests whether the doFilter method works in instances where variability
     * did change.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/git.diff");
        LineCounter counter = new LineCounter(inputFile, new ArrayList<Path>(),
            Pattern.compile("(.*.c)|(.*.h)"));
        Assert.assertThat(
            counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 932),
            CoreMatchers.equalTo(927));
        Assert.assertThat(
            counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 2950),
            CoreMatchers.equalTo(2940));
        Assert.assertThat(
            counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 4),
            CoreMatchers.equalTo(4));

    }
}
