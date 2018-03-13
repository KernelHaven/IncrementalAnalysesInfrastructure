package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private static final File MODIFIED_FOLDER = new File("testdata/incremental-pipeline/modified");
	private static final File DIFF_FILE = new File("testdata/incremental-pipeline/git.diff");

	/**
	 * Inits the logger.
	 */
	@BeforeClass
	public static void initLogger() {
		LOGGER = Logger.get();
		LOGGER.setLevel(Level.DEBUG);

	}

	/**
	 * Tests whether the filterInput method works.
	 * 
	 * @throws IOException
	 * 
	 * 
	 */
	@Test
	public void testDoFilter() throws IOException {
		ChangedOnlyFilter filter = new ChangedOnlyFilter(MODIFIED_FOLDER, DIFF_FILE, Pattern.compile(".*"));
		Collection<Path> paths = filter.getFilteredResult();
		Assert.assertThat(paths, CoreMatchers.hasItems(Paths.get("file-that-was-added.txt"),
				Paths.get("file-that-will-be-modified.txt")));

	}

}
