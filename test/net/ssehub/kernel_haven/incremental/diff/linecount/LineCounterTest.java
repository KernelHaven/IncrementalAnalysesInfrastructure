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


    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber_huge_memory() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/huge_memory-commit.diff");
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
    
    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber_ec() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/ec-commit.diff");
        LineCounter counter = new LineCounter(inputFile, new ArrayList<Path>(),
            Pattern.compile("(.*.c)|(.*.h)"));
        Assert.assertThat(
            counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1774),
            CoreMatchers.equalTo(1789));
        Assert.assertThat(
            counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1788),
            CoreMatchers.equalTo(1803));

    }
}
