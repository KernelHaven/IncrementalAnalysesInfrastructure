package net.ssehub.kernel_haven.incremental.diff.linecount;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * The Class LineCounterTest.
 * 
 * @author moritz
 */
public class LineCounterTest {

    /**
     * Test get new line number huge memory.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber_huge_memory() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/huge_memory-commit.diff");
        LineCounter counter = new LineCounter(inputFile);
        Assert.assertThat(counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 932), CoreMatchers.equalTo(927));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 2950), CoreMatchers.equalTo(2940));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 4), CoreMatchers.equalTo(4));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("mm/huge_memory.c"), 2951), CoreMatchers.equalTo(2941));

    }

    /**
     * Test get new line number ec.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber_ec() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/ec-commit.diff");
        LineCounter counter = new LineCounter(inputFile);

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 4), CoreMatchers.equalTo(4));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1598), CoreMatchers.equalTo(1598));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1694), CoreMatchers.equalTo(1705));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1695), CoreMatchers.equalTo(1707));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1774), CoreMatchers.equalTo(1789));
        
        //Those two test if a line that lies after the file end is correctly adjusted
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1788), CoreMatchers.equalTo(1803));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1800), CoreMatchers.equalTo(1815));

    }
}
