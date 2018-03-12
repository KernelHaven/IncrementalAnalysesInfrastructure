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
	public void testMerge_positive() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logDebug("Temp-Folder for testMerge: " + tempFolder);
		// Setup
		FolderUtil.copyFolderContent(ORIGINAL_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Merge action

		DiffIntegrationUtil diffIntegration = new DiffIntegrationUtil(tempFolder.toFile(), DIFF_FILE);

		boolean success = diffIntegration.mergeChanges();
		Assert.assertTrue(success);

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), MODIFIED_FOLDER));

	}

	/**
	 * Tests whether the call to git apply fails on a folder where the diff has
	 * already been applied. This is effectively the same as trying to "apply" on a
	 * folder that does not contain the files assumed by the diff-file.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testMerge_negative_alreadyMerged() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logDebug("Temp-Folder for testMerge: " + tempFolder);
		// Setup
		FolderUtil.copyFolderContent(MODIFIED_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Merge action

		DiffIntegrationUtil diffIntegration = new DiffIntegrationUtil(tempFolder.toFile(), DIFF_FILE);

		boolean success = diffIntegration.mergeChanges();

		Assert.assertFalse(success);
		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), MODIFIED_FOLDER));

	}

	/**
	 * Tests whether the call to git apply -- reverse works.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testRevert_positive() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logDebug("Temp-Folder for testRevert: " + tempFolder);

		// Setup
		FolderUtil.copyFolderContent(MODIFIED_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Revert action
		DiffIntegrationUtil diffIntegration = new DiffIntegrationUtil(tempFolder.toFile(), DIFF_FILE);

		boolean success = diffIntegration.revertChanges();
		Assert.assertTrue(success);

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), ORIGINAL_FOLDER));

	}

	/**
	 * Tests whether the call to git apply --revert fails on a folder where the diff
	 * was not applied before. This is effectively the same as trying to "apply
	 * --revert" on a folder that does not fit the diff-file.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testRevert_negative_noDiffApplied() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logDebug("Temp-Folder for testRevert: " + tempFolder);

		// Setup
		FolderUtil.copyFolderContent(ORIGINAL_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Revert action
		DiffIntegrationUtil diffIntegration = new DiffIntegrationUtil(tempFolder.toFile(), DIFF_FILE);

		boolean success = diffIntegration.revertChanges();
		Assert.assertFalse(success);

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), ORIGINAL_FOLDER));

	}
}
