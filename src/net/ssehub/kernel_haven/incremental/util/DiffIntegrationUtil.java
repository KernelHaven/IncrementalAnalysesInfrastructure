package net.ssehub.kernel_haven.incremental.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

public class DiffIntegrationUtil {
	private static final Logger LOGGER = Logger.get();

	private final File filesStorageDir;
	private final File inputDiff;

	public DiffIntegrationUtil(File filesStorageDir, File inputDiff) {
		this.filesStorageDir = filesStorageDir;
		this.inputDiff = inputDiff;
	}

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
				LOGGER.logError(("git apply stderr:\n" + stderr).split("\n"));
			} else if ((stdout != null && !stdout.equals(""))) {
				LOGGER.logDebug("git apply stout:\n" + stdout.split("\n"));
			}

		}

		return success;

	}

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
				LOGGER.logError(("git apply --reverse stderr:\n" + stderr).split("\n"));
			} else if ((stdout != null && !stdout.equals(""))) {
				LOGGER.logDebug("git apply stout:\n" + stdout.split("\n"));
			}

		}

		return success;

	}

}
