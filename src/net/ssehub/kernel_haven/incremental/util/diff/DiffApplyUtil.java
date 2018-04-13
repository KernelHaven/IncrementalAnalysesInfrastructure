package net.ssehub.kernel_haven.incremental.util.diff;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * The Class DiffIntegrationUtil.
 * 
 * @author Moritz
 */
public class DiffApplyUtil {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.get();

	/** The files storage dir. */
	private final File filesStorageDir;
	
	/** The input diff. */
	private final File inputDiff;

	/**
	 * Instantiates a new diff integration util.
	 *
	 * @param filesStorageDir the files storage dir
	 * @param inputDiff the input diff
	 */
	public DiffApplyUtil(File filesStorageDir, File inputDiff) {
		this.filesStorageDir = filesStorageDir;
		this.inputDiff = inputDiff;
	}

	/**
	 * Merge changes based on the input folder and provided diff.
	 *
	 * @return true, if successful
	 */
	public boolean mergeChanges() {
		LOGGER.logDebug("mergeChanges() called");

		boolean success = false;

		if (filesStorageDir.isDirectory() && inputDiff.isFile()) {
			ProcessBuilder processBuilder = new ProcessBuilder("git", "apply", inputDiff.getAbsolutePath());
			processBuilder.directory(filesStorageDir);

			ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
			ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

			try {
				success = Util.executeProcess(processBuilder, "git apply", stdoutStream, stderrStream, 0);
			} catch (IOException e) {
				LOGGER.logException("Could not merge changes", e);
			}

			String stderr = stderrStream.toString();
			String stdout = stdoutStream.toString();
			if (stderr != null && !stderr.equals("")) {
				if (!success) {
					LOGGER.logError(("git apply stderr:\n" + stderr).split("\n"));
				} else {
					LOGGER.logDebug(("git apply stderr:\n" + stderr).split("\n"));	
				}
			} 
			
			if ((stdout != null && !stdout.equals(""))) {
				LOGGER.logDebug(("git apply stout:\n" + stdout).split("\n"));
			}

		}

		return success;

	}

	/**
	 * Revert changes based on the input folder and provided diff.
	 *
	 * @return true, if successful
	 */
	public boolean revertChanges() {
		LOGGER.logDebug("revertChanges() called");
		boolean success = false;

		if (filesStorageDir.isDirectory() && inputDiff.isFile()) {
			ProcessBuilder processBuilder = new ProcessBuilder("git", "apply", "--reverse",
					inputDiff.getAbsolutePath());
			processBuilder.directory(filesStorageDir);

			ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
			ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

			try {
				success = Util.executeProcess(processBuilder, "git apply --reverse", stdoutStream, stderrStream, 0);
			} catch (IOException e) {
				LOGGER.logException("Could not revert changes", e);
			}

			String stderr = stderrStream.toString();
			String stdout = stdoutStream.toString();
			if (stderr != null && !stderr.equals("")) {
				if (!success) {
					LOGGER.logError(("git apply --reverse stderr:\n" + stderr).split("\n"));
				} else {
					LOGGER.logDebug(("git apply --reverse stderr:\n" + stderr).split("\n"));	
				}
			} 
			
			if ((stdout != null && !stdout.equals(""))) {
				LOGGER.logDebug(("git apply --reverse stout:\n" + stdout).split("\n"));
			}

		}

		return success;

	}

}
