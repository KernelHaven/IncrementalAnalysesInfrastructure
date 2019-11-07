package net.ssehub.kernel_haven.incremental.diff.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

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
     * Tests if the correct number of Lines-Types is present for each category.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_correctNumberOfLineTypeÓccurences_huge_memory() throws IOException {
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
     * Test if lines elements occur in the same order as expected. 
     * This tests also checks the size of some of those elements.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_correctOrderOfLineTypeOccurences_ec() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/ec-commit.diff");
        DiffFile diffFile = DiffFileParser.parse(inputFile);

        List<Lines> linesList = diffFile.getEntry(Paths.get("drivers/acpi/ec.c")).getLines();
        Assert.assertThat(linesList.get(0).getType(), CoreMatchers.equalTo(Lines.LineType.BETWEEN_CHUNKS));
        Assert.assertThat(linesList.get(0).getCount() , CoreMatchers.equalTo(1595));
        Assert.assertThat(linesList.get(1).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(1).getCount() , CoreMatchers.equalTo(4));
        Assert.assertThat(linesList.get(0).getCount() + linesList.get(1).getCount() , CoreMatchers.equalTo(1599));
        Assert.assertThat(linesList.get(2).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(3).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(4).getType(), CoreMatchers.equalTo(Lines.LineType.DELETED));
        Assert.assertThat(linesList.get(5).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(6).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(7).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(8).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(9).getType(), CoreMatchers.equalTo(Lines.LineType.DELETED));
        Assert.assertThat(linesList.get(10).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(11).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(12).getType(), CoreMatchers.equalTo(Lines.LineType.BETWEEN_CHUNKS));
        Assert.assertThat(linesList.get(13).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(14).getType(), CoreMatchers.equalTo(Lines.LineType.DELETED));
        Assert.assertThat(linesList.get(15).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(16).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(17).getType(), CoreMatchers.equalTo(Lines.LineType.BETWEEN_CHUNKS));
        Assert.assertThat(linesList.get(18).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(19).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(19).getCount(), CoreMatchers.equalTo(1));
        
        Assert.assertThat(linesList.get(20).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(21).getType(), CoreMatchers.equalTo(Lines.LineType.BETWEEN_CHUNKS));
        Assert.assertThat(linesList.get(22).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(22).getCount(), CoreMatchers.equalTo(4));
        Assert.assertThat(linesList.get(23).getType(), CoreMatchers.equalTo(Lines.LineType.DELETED));
        Assert.assertThat(linesList.get(23).getCount(), CoreMatchers.equalTo(5));
        Assert.assertThat(linesList.get(24).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(24).getCount(), CoreMatchers.equalTo(8));
        Assert.assertThat(linesList.get(25).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(25).getCount(), CoreMatchers.equalTo(3));
        Assert.assertThat(linesList.get(26).getType(), CoreMatchers.equalTo(Lines.LineType.BETWEEN_CHUNKS));
        
        Assert.assertThat(linesList.get(27).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(linesList.get(27).getCount(), CoreMatchers.equalTo(4));
        
        Assert.assertThat(linesList.get(28).getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(linesList.get(29).getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
    }
    
    
    /**
     * Tests if the correct number of Lines-Types is present for each category.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_correctNumberOfLineTypeÓccurences_ec() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/ec-commit.diff");
        DiffFile diffFile = DiffFileParser.parse(inputFile);

        int betweenChunksCount = 0;
        int deletedCount = 0;
        int addedCount = 0;
        int unmodifiedCount = 0;
        for (Lines lines : diffFile.getEntry(Paths.get("drivers/acpi/ec.c")).getLines()) {
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
        Assert.assertThat(deletedCount, CoreMatchers.equalTo(4));
        Assert.assertThat(addedCount, CoreMatchers.equalTo(8));
        Assert.assertThat(unmodifiedCount, CoreMatchers.equalTo(13));

    }
    
    /**
     * Tests if the correct number of Lines-Types is present for each category.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_correctLineTypeOccurences_ds2490() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File("testdata/lines/ds2490-commit.diff");
        DiffFile diffFile = DiffFileParser.parse(inputFile);
        List<Lines> linesList = diffFile.getEntry(Paths.get("drivers/w1/masters/ds2490.c")).getLines();
        Lines firstLines = linesList.get(0);
        Assert.assertThat(firstLines.getType(), CoreMatchers.equalTo(Lines.LineType.UNMODIFIED));
        Assert.assertThat(firstLines.getCount(), CoreMatchers.equalTo(155));
        Lines secondLines = linesList.get(1);
        Assert.assertThat(secondLines.getType(), CoreMatchers.equalTo(Lines.LineType.ADDED));
        Assert.assertThat(secondLines.getCount(), CoreMatchers.equalTo(3));

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
