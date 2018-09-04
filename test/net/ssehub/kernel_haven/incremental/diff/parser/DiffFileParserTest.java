package net.ssehub.kernel_haven.incremental.diff.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Lines;
import net.ssehub.kernel_haven.incremental.util.PosixUtil;


/**
 * The Class DiffFileParserTest.
 * @author Moritz
 */
public class DiffFileParserTest {

    /**
     * Test parse lines elements.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_linesElements() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/huge_memory-commit.diff");
        DiffFile diffFile = DiffFileParser.parse(inputFile);

        int betweenChunksCount = 0;
        int deletedCount = 0;
        int addedCount = 0;
        int unmodifiedCount = 0;
        for (Lines lines : diffFile.getEntry(Paths.get("mm/huge_memory.c")).getLines()) {
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

    /**
     * Test parse lines elements.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_filePermissions() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/huge_memory-commit.diff");
        DiffFile diffFile = DiffFileParser.parse(inputFile);
        Assert.assertThat(diffFile.getEntry(Paths.get("mm/huge_memory.c")).getPermissions(),
                CoreMatchers.equalTo(PosixUtil.getPosixFilePermissionForNumberString("0644")));

    }

}
