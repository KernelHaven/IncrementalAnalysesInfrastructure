package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Tests for {@link VariabilityDiffAnalyzer}.
 * 
 * @author moritz
 */
public class VariabilityDiffAnalyzerTest {

    /** The logger. */
    private static final Logger LOGGER = Logger.get();

    /**
     * Tests whether the doFilter method works in instances where variability
     * did not change.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_modification_no_variability_change()
        throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File(
            "testdata/variability-changes/no-variability-changes.diff");
        DiffFile diffFile =
            new VariabilityDiffAnalyzer().generateDiffFile(inputFile);
        LOGGER.logInfo("The following entries were found: ");
        diffFile.getEntries()
            .forEach(entry -> LOGGER.logInfo(entry.toString()));

        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers.hasItem(new FileEntry(Paths.get("modify/Kbuild"),
                FileEntry.FileChange.MODIFICATION,
                FileEntry.VariabilityChange.NO_CHANGE, null)));
        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers.hasItem(new FileEntry(Paths.get("modify/Kconfig"),
                FileEntry.FileChange.MODIFICATION,
                FileEntry.VariabilityChange.NO_CHANGE, null)));
        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers.hasItem(new FileEntry(
                Paths.get("modify/a-code-file.c"), FileEntry.FileChange.MODIFICATION,
                FileEntry.VariabilityChange.NO_CHANGE, null)));

    }

    /**
     * Tests whether the doFilter method works in instances where variability
     * did change.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_variability_change() throws IOException {
        // CHECKSTYLE:ON
        File inputFile = new File(
            "testdata/variability-changes/some-variability-changes.diff");
        DiffFile diffFile =
            new VariabilityDiffAnalyzer().generateDiffFile(inputFile);
        LOGGER.logInfo("The following entries were found: ");
        diffFile.getEntries()
            .forEach(entry -> LOGGER.logInfo(entry.toString()));

        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers
                .hasItem(new FileEntry(Paths.get("include/linux/compat.h"),
                    FileEntry.FileChange.MODIFICATION,
                    FileEntry.VariabilityChange.NO_CHANGE, null)));
        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers.hasItem(
                new FileEntry(Paths.get(".mailmap"), FileEntry.FileChange.DELETION,
                    FileEntry.VariabilityChange.NOT_A_VARIABILITY_FILE, null)));
        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers
                .hasItem(new FileEntry(Paths.get("include/linux/bitmap.h"),
                    FileEntry.FileChange.MODIFICATION,
                    FileEntry.VariabilityChange.NO_CHANGE, null)));
        Assert.assertThat(diffFile.getEntries(),
            CoreMatchers
                .hasItem(new FileEntry(Paths.get("drivers/crypto/caam/ctrl.c"),
                    FileEntry.FileChange.MODIFICATION,
                    FileEntry.VariabilityChange.CHANGE, null)));

    }

}
