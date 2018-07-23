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

import net.ssehub.kernel_haven.incremental.diff.linecount.LineInfoExtractor.Lines;

/**
 * The Class LineParserTest.
 */
public class LineInfoExtractorTest {

    /**
     * Tests whether the doFilter method works in instances where variability
     * did change.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_linesElements() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/git.diff");
        LineInfoExtractor parser = new LineInfoExtractor(inputFile,
            new ArrayList<Path>(), Pattern.compile("(.*.c)|(.*.h)"));

        int betweenChunksCount = 0;
        int deletedCount = 0;
        int addedCount = 0;
        int unmodifiedCount = 0;
        for (Lines lines : parser.getLines(Paths.get("mm/huge_memory.c"))) {
            if (lines.getType().equals(Lines.LineType.BETWEEN_CHUNKS)) {
                betweenChunksCount++;
            } else if (lines.getType().equals(Lines.LineType.DELETED)) {
                deletedCount++;
            } else if (lines.getType().equals(Lines.LineType.ADDED)) {
                addedCount++;
            } else if (lines.getType().equals(Lines.LineType.UNMODIFIED)) {
                unmodifiedCount++;
            }
        }
        Assert.assertThat(betweenChunksCount, CoreMatchers.equalTo(5));
        Assert.assertThat(deletedCount, CoreMatchers.equalTo(9));
        Assert.assertThat(addedCount, CoreMatchers.equalTo(9));
        Assert.assertThat(unmodifiedCount, CoreMatchers.equalTo(14));

    }

}
