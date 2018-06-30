package net.ssehub.kernel_haven.incremental.diff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.analyzer.SimpleDiffAnalyzer;
import net.ssehub.kernel_haven.incremental.util.FileUtil;

// TODO: Auto-generated Javadoc
/**
 * Tests for {@link DiffFile}.
 *
 * @author moritz
 */
public class DiffFileTest {


    /** Path to git-diff file. */
    private static final File GIT_DIFF =
        new File("testdata/diff-file/git.diff");

    /** Path to parsed and serialized version of git-diff file. */
    private static final File DIFF_FILE =
        new File("testdata/diff-file/diff_file.test");

    // CHECKSTYLE:OFF
    /**
     * Tests whether the doFilter method works.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetEntries_modification() throws IOException {
        DiffFile diffFile = new SimpleDiffAnalyzer().generateDiffFile(GIT_DIFF);
        Collection<Path> paths = new ArrayList<Path>();
        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileEntry.Type.MODIFICATION)) {
                paths.add(entry.getPath());
            }
        }
        Assert.assertThat(paths.size(), CoreMatchers.equalTo(3));
        Assert.assertThat(paths,
            CoreMatchers.hasItem(Paths.get("modify/Kbuild")));
        Assert.assertThat(paths,
            CoreMatchers.hasItem(Paths.get("modify/Kconfig")));
        Assert.assertThat(paths,
            CoreMatchers.hasItem(Paths.get("modify/a-code-file.c")));
    }
    // CHECKSTYLE:ON

    /**
     * Test save.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JAXBException
     *             the JAXB exception
     */
    @Test
    public void testSave() throws IOException, JAXBException {
        DiffFile diffFile = new SimpleDiffAnalyzer().generateDiffFile(GIT_DIFF);
        File file = Files.createTempFile("git-diff", "temp").toFile();
        file.deleteOnExit();
        diffFile.save(file);
        Assert.assertTrue(!FileUtil.readFile(file).isEmpty());
    }

    /**
     * Test load.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JAXBException
     *             the JAXB exception
     * @throws ParseException
     *             the parse exception
     */
    @Test
    public void testLoad() throws IOException, JAXBException, ParseException {
        DiffFile referenceDiffFile =
            new SimpleDiffAnalyzer().generateDiffFile(GIT_DIFF);

        DiffFile loadedDiffFile = DiffFile.load(DIFF_FILE);

        Assert.assertThat(loadedDiffFile,
            CoreMatchers.equalTo(referenceDiffFile));
    }
}
