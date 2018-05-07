package net.ssehub.kernel_haven.incremental.preparation.filter;

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

import net.ssehub.kernel_haven.incremental.preparation.filter.VariabilityChangesFilter;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.analyzer.VariabilityDiffAnalyzer;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;


/**
 * The Class VariabilityChangesFilterTest.
 * @author moritz
 */
public class VariabilityChangesFilterTest {

	/** The logger. */
	private static Logger LOGGER = null;


	/**
	 * Inits the logger.
	 */
	@BeforeClass
	public static void initLogger() {
		LOGGER = Logger.get();
		LOGGER.setLevel(Level.DEBUG);
	}

	/**
	 * Tests whether the doFilter method works if a file was completely changed.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testDoFilter_variability_change() throws IOException {
		DiffFile diffFile = VariabilityDiffAnalyzer.generateDiffFile(new File("testdata/variability-changes/some-variability-changes.diff"));
		VariabilityChangesFilter filter = new VariabilityChangesFilter(null, diffFile, Pattern.compile(".*"));
		Collection<Path> paths = filter.getFilteredResult();
		Assert.assertThat(paths, CoreMatchers.hasItem(Paths.get("drivers/crypto/caam/ctrl.c")));


	}

}