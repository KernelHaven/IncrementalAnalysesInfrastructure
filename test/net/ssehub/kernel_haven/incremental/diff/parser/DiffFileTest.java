package net.ssehub.kernel_haven.incremental.diff.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * Tests for {@link DiffFile}.
 *
 * @author moritz
 */
public class DiffFileTest {

    /** Path to git-diff file. */
    private static final File GIT_DIFF = new File("testdata/diff-file/git.diff");

    // CHECKSTYLE:OFF
    /**
     * Tests whether the doFilter method works.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetEntries_modification() throws IOException {
        DiffFile diffFile = DiffFileParser.parse(GIT_DIFF);
        Collection<Path> paths = new ArrayList<Path>();

        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileEntry.FileChange.MODIFICATION)) {
                paths.add(entry.getPath());
            }
        }
        Assert.assertThat(paths.size(), CoreMatchers.equalTo(3));
        Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kbuild")));
        Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kconfig")));
        Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/a-code-file.c")));
    }
    // CHECKSTYLE:ON

}
