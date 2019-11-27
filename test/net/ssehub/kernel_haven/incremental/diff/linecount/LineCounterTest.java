package net.ssehub.kernel_haven.incremental.diff.linecount;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;

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
        LineCounter counter = new LineCounter(DiffFileParser.parse(inputFile));
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
        LineCounter counter = new LineCounter(DiffFileParser.parse(inputFile));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 4), CoreMatchers.equalTo(4));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1598), CoreMatchers.equalTo(1598));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1694), CoreMatchers.equalTo(1705));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1695), CoreMatchers.equalTo(1707));
        
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1696), CoreMatchers.equalTo(1708));

        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1774), CoreMatchers.equalTo(1789));

        // Those two test if a line that lies after the file end is correctly adjusted
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1788), CoreMatchers.equalTo(1803));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/acpi/ec.c"), 1800), CoreMatchers.equalTo(1815));

    }
    
    /**
     * Test get new line number for ds2490.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber_ds2490() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/ds2490-commit.diff");
        LineCounter counter = new LineCounter(DiffFileParser.parse(inputFile));
        
        //Test line right before first addition
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/w1/masters/ds2490.c"), 155),
                CoreMatchers.equalTo(155));
        //Test line right after first addition
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/w1/masters/ds2490.c"), 156),
                CoreMatchers.equalTo(159));
        
        //Test problematic line in result
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/w1/masters/ds2490.c"), 799),
                CoreMatchers.equalTo(803));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("drivers/w1/masters/ds2490.c"), 1090),
                CoreMatchers.equalTo(1115));


    }
    
    
    /**
     * Test get new line number for clock.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testGetNewLineNumber_clock() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/clock-commit.diff");
        LineCounter counter = new LineCounter(DiffFileParser.parse(inputFile));
        
        //Test line right before first addition
        Assert.assertThat(counter.getNewLineNumber(Paths.get("kernel/sched/clock.c"), 368), CoreMatchers.equalTo(380));
        //Test line right after first addition
        Assert.assertThat(counter.getNewLineNumber(Paths.get("kernel/sched/clock.c"), 374), CoreMatchers.equalTo(381));
        
        //Test problematic line in result
        Assert.assertThat(counter.getNewLineNumber(Paths.get("kernel/sched/clock.c"), 380), CoreMatchers.equalTo(387));
        Assert.assertThat(counter.getNewLineNumber(Paths.get("kernel/sched/clock.c"), 381), CoreMatchers.equalTo(389));
        
    }
}
