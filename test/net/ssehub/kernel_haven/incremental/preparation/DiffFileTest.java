package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.util.FileUtil;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;
import net.ssehub.kernel_haven.incremental.util.diff.analyzer.SimpleDiffAnalyzer;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

/**
 * The Class DiffFileTest.
 * 
 * @author moritz
 */
public class DiffFileTest {

	/** The logger. */
	private static Logger LOGGER = null;

	/** The Constant DIFF_FILE. */
	private static final File GIT_DIFF = new File("testdata/diff-file/git.diff");
	private static final File DIFF_FILE = new File("testdata/diff-file/diff_file.test");

	/**
	 * Inits the logger.
	 */
	@BeforeClass
	public static void initLogger() {
		LOGGER = Logger.get();
		LOGGER.setLevel(Level.DEBUG);
	}

	/**
	 * Tests whether the doFilter method works.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testGetEntries_modification() throws IOException {
		DiffFile diffFile = SimpleDiffAnalyzer.generateDiffFile(GIT_DIFF);
		Collection<Path> paths = new ArrayList<Path>();
		for (FileEntry entry : diffFile.getEntries()) {
			if (entry.getType().equals(FileEntry.Type.MODIFICATION)) {
				paths.add(entry.getPath());
			}
		}
		Assert.assertThat(paths.size(), CoreMatchers.equalTo(3));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kbuild")));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kconfig")));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/a-code-file.c")));
	}

	@Test
	public void testSave() throws IOException, JAXBException {
		DiffFile diffFile = SimpleDiffAnalyzer.generateDiffFile(GIT_DIFF);
		File file = Files.createTempFile("git-diff", "temp").toFile();
		file.deleteOnExit();
		diffFile.save(file);
		Assert.assertTrue(!FileUtil.readFile(file).isEmpty());
	}

	@Test
	public void testLoad() throws IOException, JAXBException {
		DiffFile referenceDiffFile = SimpleDiffAnalyzer.generateDiffFile(GIT_DIFF);
		DiffFile loadedDiffFile = DiffFile.load(DIFF_FILE);

		Assert.assertThat(loadedDiffFile, CoreMatchers.equalTo(referenceDiffFile));
	}
}
