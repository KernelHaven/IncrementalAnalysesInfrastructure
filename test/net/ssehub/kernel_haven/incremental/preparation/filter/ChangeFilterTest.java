package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Tests for {@link ChangeFilter}.
 * 
 * @author moritz
 */
public class ChangeFilterTest {

    /** The logger. */
    private static final Logger LOGGER = Logger.get();

    /** The Constant MODIFIED_FOLDER. */
    private static final File MODIFIED_FOLDER = new File("testdata/changed-only/modified");

    /** The Constant DIFF_FILE. */
    private static final File DIFF_FILE = new File("testdata/changed-only/git.diff");

    /**
     * Tests whether the doFilter method works.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDoFilter() throws IOException {

        DiffFile diffFile = DiffFileParser.parse(DIFF_FILE);

        ChangeFilter filter = new ChangeFilter(MODIFIED_FOLDER, diffFile, Pattern.compile(".*"), false);
        Collection<Path> paths = filter.getFilteredResult();
        LOGGER.logInfo(Arrays.toString(paths.toArray()));
        Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kbuild")));
        Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kconfig")));
        Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/a-code-file.c")));

    }

}
