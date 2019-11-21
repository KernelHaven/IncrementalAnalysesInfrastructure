package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.diff.applier.FileReplacingDiffApplier;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.util.FileUtil;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Tests for {@link FileReplacingDiffApplier}.
 * 
 * @author moritz
 */
public class FileReplacingDiffApplierTest {

	/** The Constant ORIGINAL_FOLDER. */
	private static final File ORIGINAL_FOLDER = new File("testdata/diff-integration/original");

	/** The Constant MODIFIED_FOLDER. */
	private static final File MODIFIED_FOLDER = new File("testdata/diff-integration/modified");

	/** The Constant DIFF_FILE. */
	private static final File DIFF_FILE = new File("testdata/diff-integration/git.diff");

	/** The logger. */
	private static final Logger LOGGER = Logger.get();

	// CHECKSTYLE:OFF
	/**
	 * Tests whether the call to git apply works.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testMerge_positive() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logInfo("Temp-Folder for testMerge: " + tempFolder);
		// Setup
		FolderUtil.copyFolderContent(ORIGINAL_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());
		Assert.assertTrue(!FolderUtil.folderContentEquals(tempFolder.toFile(), MODIFIED_FOLDER));

		// Merge action

		FileReplacingDiffApplier diffIntegration = new FileReplacingDiffApplier(tempFolder.toFile(),
				DiffFileParser.parse(DIFF_FILE));

		boolean success = diffIntegration.mergeChanges();
		Assert.assertTrue(success);

		Collection<File> filesInTemp = FolderUtil.listRelativeFiles(tempFolder.toFile(), true);
		Collection<File> filesInRef = FolderUtil.listRelativeFiles(MODIFIED_FOLDER, true);
		Assert.assertTrue("Collections were expected to contain the same items but did not: \nfilesInTemp:"
				+ Arrays.toString(filesInTemp.toArray()) + "\nfilesInRef:" + Arrays.toString(filesInRef.toArray()),
				filesInTemp.containsAll(filesInRef) && filesInRef.containsAll(filesInTemp));

		Assert.assertThat(FileUtil.readFile(tempFolder.resolve("file-that-was-added.txt").toFile()), CoreMatchers
				.equalTo(FileUtil.readFile(MODIFIED_FOLDER.toPath().resolve("file-that-was-added.txt").toFile())));
		Assert.assertThat(FileUtil.readFile(tempFolder.resolve("file-that-will-be-kept.txt").toFile()), CoreMatchers
				.equalTo(FileUtil.readFile(MODIFIED_FOLDER.toPath().resolve("file-that-will-be-kept.txt").toFile())));
		Assert.assertThat(FileUtil.readFile(tempFolder.resolve("file-that-will-be-modified.txt").toFile()),
				CoreMatchers.equalTo(FileUtil
						.readFile(MODIFIED_FOLDER.toPath().resolve("file-that-will-be-modified.txt").toFile())));

	}
	// CHECKSTYLE:OFF

	/**
	 * Tests whether the call to git apply fails on a folder where the diff has
	 * already been applied. This is effectively the same as trying to "apply" on a
	 * folder that does not contain the files assumed by the diff-file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testMerge_negative_alreadyMerged() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logInfo("Temp-Folder for testMerge: " + tempFolder);
		// Setup
		FolderUtil.copyFolderContent(MODIFIED_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Merge action

		FileReplacingDiffApplier diffIntegration = new FileReplacingDiffApplier(tempFolder.toFile(),
				DiffFileParser.parse(DIFF_FILE));

		boolean success = diffIntegration.mergeChanges();

		Assert.assertFalse(success);
		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), MODIFIED_FOLDER));

	}
	// CHECKSTYLE:ON

	// CHECKSTYLE:OFF
	/**
	 * Tests whether the call to git apply -- reverse works.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRevert_positive() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logInfo("Temp-Folder for testRevert: " + tempFolder);

		// Setup
		FolderUtil.copyFolderContent(MODIFIED_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Revert action
		FileReplacingDiffApplier diffIntegration = new FileReplacingDiffApplier(tempFolder.toFile(),
				DiffFileParser.parse(DIFF_FILE));

		boolean success = diffIntegration.revertChanges();
		Assert.assertTrue("Merge successful ", success);

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), ORIGINAL_FOLDER));

	}
	// CHECKSTYLE:ON

	// CHECKSTYLE:OFF
	/**
	 * Tests whether the call to git apply --revert fails on a folder where the diff
	 * was not applied before. This is effectively the same as trying to "apply
	 * --revert" on a folder that does not fit the diff-file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRevert_negative_noDiffApplied() throws IOException {
		Path tempFolder = Files.createTempDirectory("git-diff-apply-test");
		LOGGER.logInfo("Temp-Folder for testRevert: " + tempFolder);

		// Setup
		FolderUtil.copyFolderContent(ORIGINAL_FOLDER, tempFolder.toFile());

		// Check preconditions
		Assert.assertTrue(tempFolder.toFile().exists());
		Assert.assertTrue(DIFF_FILE.exists());

		// Revert action
		FileReplacingDiffApplier diffIntegration = new FileReplacingDiffApplier(tempFolder.toFile(),
				DiffFileParser.parse(DIFF_FILE));

		boolean success = diffIntegration.revertChanges();
		Assert.assertFalse(success);

		Assert.assertTrue(FolderUtil.folderContentEquals(tempFolder.toFile(), ORIGINAL_FOLDER));

	}
	// CHECKSTYLE:ON
}
