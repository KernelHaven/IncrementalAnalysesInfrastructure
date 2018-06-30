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
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.analyzer.VariabilityDiffAnalyzer;
import net.ssehub.kernel_haven.incremental.preparation.filter.ChangeFilter;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

/**
 * Tests for {@link ChangeFilter}
 * @author moritz
 */
public class ChangeFilterTest {

	/** The logger. */
	private static Logger LOGGER = null;
	
	/** The Constant MODIFIED_FOLDER. */
	private static final File MODIFIED_FOLDER = new File("testdata/changed-only/modified");
	
	/** The Constant DIFF_FILE. */
	private static final File DIFF_FILE = new File("testdata/changed-only/git.diff");

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
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testDoFilter() throws IOException {
		ChangeFilter filter = new ChangeFilter(MODIFIED_FOLDER, new VariabilityDiffAnalyzer().generateDiffFile(DIFF_FILE), Pattern.compile(".*"), false);
		Collection<Path> paths = filter.getFilteredResult();
		LOGGER.logDebug(Arrays.toString(paths.toArray()));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kbuild")));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kconfig")));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/a-code-file.c")));

	}

}
