package net.ssehub.kernel_haven.incremental.util;

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

import net.ssehub.kernel_haven.incremental.preparation.filter.ChangedOnlyFilter;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;
import net.ssehub.kernel_haven.incremental.util.diff.analyzer.ComAnDiffAnalyzer;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

public class ComAnDiffAnalyzerTest {

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
	 * Tests whether the doFilter method works.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testParse_modification_no_variability_change() throws IOException {
		File inputFile = new File("testdata/variability-changes/no-variability-changes.diff");
		DiffFile diffFile = new DiffFile(new ComAnDiffAnalyzer(inputFile));
		LOGGER.logDebug("The following entries were found: ");
		diffFile.getEntries().forEach(entry -> LOGGER.logDebug(entry.toString()));

		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get("modify/Kbuild"), 
				FileEntry.Type.MODIFICATION, FileEntry.VariabilityChange.NO_CHANGE)));
		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get("modify/Kconfig"), 
				FileEntry.Type.MODIFICATION, FileEntry.VariabilityChange.NO_CHANGE)));
		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get("modify/a-code-file.c"), 
				FileEntry.Type.MODIFICATION, FileEntry.VariabilityChange.NO_CHANGE)));

	}
	
	/**
	 * Tests whether the doFilter method works.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testParse_variability_change() throws IOException {
		File inputFile = new File("testdata/variability-changes/some-variability-changes.diff");
		DiffFile diffFile = new DiffFile(new ComAnDiffAnalyzer(inputFile));
		LOGGER.logDebug("The following entries were found: ");
		diffFile.getEntries().forEach(entry -> LOGGER.logDebug(entry.toString()));

		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get("include/linux/compat.h"), 
				FileEntry.Type.MODIFICATION, FileEntry.VariabilityChange.NO_CHANGE)));
		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get(".mailmap"), 
				FileEntry.Type.DELETION, FileEntry.VariabilityChange.NOT_A_VARIABILITY_FILE)));
		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get("include/linux/bitmap.h"), 
				FileEntry.Type.MODIFICATION, FileEntry.VariabilityChange.NO_CHANGE)));
		Assert.assertThat(diffFile.getEntries(), CoreMatchers.hasItem(new FileEntry(Paths.get("drivers/crypto/caam/ctrl.c"), 
				FileEntry.Type.MODIFICATION, FileEntry.VariabilityChange.CHANGE)));

	}


}
