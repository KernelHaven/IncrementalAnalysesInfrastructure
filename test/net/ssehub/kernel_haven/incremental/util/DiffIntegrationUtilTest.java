package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

public class DiffIntegrationUtilTest {

	private static final File ORIGINAL_FOLDER = new File("testdata/diff-integration/original");
	private static final File MODIFIED_FOLDER = new File("testdata/diff-integration/modified");
	private static final File DIFF_FILE = new File("testdata/diff-integration/git.diff");
	private static Logger LOGGER = null;

	/**
	 * Inits the logger.
	 */
	@BeforeClass
	public static void initLogger() {
		Logger.init();
		LOGGER = Logger.get();
		LOGGER.setLevel(Level.DEBUG);

	}

	/**
	 * Tests whether the call to git apply works.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testMerge() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logDebug("Temp-Folder for testMerge: " + tempFolder);
		// Setup
		FolderUtil.copyFolderContent(ORIGINAL_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Merge action

		DiffIntegrationUtil diffIntegration = new DiffIntegrationUtil(tempFolder.toFile(), DIFF_FILE);

		diffIntegration.mergeChanges();

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), MODIFIED_FOLDER));

	}

	/**
	 * Tests whether the call to git apply -- reverse works.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testRevert() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logDebug("Temp-Folder for testRevert: " + tempFolder);

		// Setup
		FolderUtil.copyFolderContent(MODIFIED_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Revert action
		DiffIntegrationUtil diffIntegration = new DiffIntegrationUtil(tempFolder.toFile(), DIFF_FILE);

		diffIntegration.revertChanges();

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), ORIGINAL_FOLDER));

	}
}
