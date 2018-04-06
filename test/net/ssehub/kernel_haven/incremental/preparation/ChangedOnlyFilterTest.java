package net.ssehub.kernel_haven.incremental.preparation;

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

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

public class ChangedOnlyFilterTest {

	private static Logger LOGGER = null;
	private static final File MODIFIED_FOLDER = new File("testdata/changed-only/modified");
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
	 * @throws IOException
	 */
	@Test
	public void testDoFilter() throws IOException {
		ChangedOnlyFilter filter = new ChangedOnlyFilter(MODIFIED_FOLDER, DIFF_FILE, Pattern.compile(".*"));
		Collection<Path> paths = filter.getFilteredResult();
		LOGGER.logDebug(Arrays.toString(paths.toArray()));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kbuild")));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/Kconfig")));
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("modify/a-code-file.c")));

	}

}
