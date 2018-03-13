package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

public class IncrementalPreparationTest extends IncrementalPreparation {

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
	 * 
	 */
	@Test
	public void testFilterInput() {
		try {
			this.filterInput("net.ssehub.kernel_haven.incremental.preparation.BogusFilter", MODIFIED_FOLDER, DIFF_FILE,
					Pattern.compile(".*"));
		} catch (SetUpException e) {
			Assert.fail("the filterInput method did not terminate properly: " + e.getMessage());
		}
	}

}
