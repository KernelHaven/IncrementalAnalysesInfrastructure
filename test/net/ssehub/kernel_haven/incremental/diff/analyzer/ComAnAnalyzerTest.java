package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Tests for {@link ComAnAnalyzer}.
 * 
 * @author moritz
 */
public class ComAnAnalyzerTest {

    /** The logger. */
    private static final Logger LOGGER = Logger.get();

    /**
     * Tests whether the doFilter method works in instances where variability did
     * not change.
     *
     * @throws IOException    Signals that an I/O exception has occurred.
     * @throws SetUpException
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_modification_no_variability_change() throws IOException, SetUpException {
        // CHECKSTYLE:ON
        DiffFile diffFile = DiffFileParser.parse(new File("testdata/variability-changes/no-variability-changes.diff"));
        ComAnAnalyzer analyzer = new ComAnAnalyzer();

        Configuration config = new Configuration(new File("testdata/variability-changes/no-configuration.properties"));
        config.registerSetting(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
        analyzer.analyzeDiffFile(diffFile, config);
        LOGGER.logInfo("The following entries were found: ");
        diffFile.getEntries().forEach(entry -> LOGGER.logInfo(entry.toString()));

        Assert.assertThat(diffFile.getEntry(Paths.get("modify/Kbuild")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.NO_CHANGE));
        Assert.assertThat(diffFile.getEntry(Paths.get("modify/Kconfig")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.NO_CHANGE));
        Assert.assertThat(diffFile.getEntry(Paths.get("modify/a-code-file.c")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.NO_CHANGE));

    }

    /**
     * Tests whether the doFilter method works in instances where variability did
     * change.
     *
     * @throws IOException    Signals that an I/O exception has occurred.
     * @throws SetUpException
     */
    @Test
    // CHECKSTYLE:OFF
    public void testParse_variability_change() throws IOException, SetUpException {
        // CHECKSTYLE:ON
        DiffFile diffFile = DiffFileParser
                .parse(new File("testdata/variability-changes/some-variability-changes.diff"));
        ComAnAnalyzer analyzer = new ComAnAnalyzer();

        Configuration config = new Configuration(
                new File("testdata/variability-changes/some-configuration.properties"));
        config.registerSetting(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
        analyzer.analyzeDiffFile(diffFile, config);
        LOGGER.logInfo("The following entries were found: ");
        diffFile.getEntries().forEach(entry -> LOGGER.logInfo(entry.toString()));

        Assert.assertThat(diffFile.getEntry(Paths.get("include/linux/compat.h")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.NO_CHANGE));
        Assert.assertThat(diffFile.getEntry(Paths.get(".mailmap")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.NOT_A_VARIABILITY_FILE));
        Assert.assertThat(diffFile.getEntry(Paths.get("include/linux/bitmap.h")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.NO_CHANGE));
        Assert.assertThat(diffFile.getEntry(Paths.get("drivers/crypto/caam/ctrl.c")).getVariabilityChange(),
                CoreMatchers.equalTo(FileEntry.VariabilityChange.CHANGE));

    }

}
