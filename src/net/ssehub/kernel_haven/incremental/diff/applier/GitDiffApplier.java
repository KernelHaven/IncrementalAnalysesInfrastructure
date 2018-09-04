package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Helper class used as an interface to "git apply". Requires git to be
 * installed on the system.
 * 
 * @author Moritz
 */
@Deprecated
public class GitDiffApplier implements DiffApplier {

    private static final Logger LOGGER = Logger.get();

    /** The files storage dir. */
    private final File filesStorageDir;

    /** The input diff. */
    private final File inputDiff;

    /**
     * Instantiates a new {@link DiffApplyUtil}.
     *
     * @param filesStorageDir the files storage dir
     * @param inputDiff       the input diff
     */
    public GitDiffApplier(File filesStorageDir, File inputDiff) {
        this.filesStorageDir = filesStorageDir;
        this.inputDiff = inputDiff;
    }

    /**
     * Merge changes based on the input folder and provided diff.
     *
     * @return true, if successful
     */
    public boolean mergeChanges() {
        boolean success = false;

        if (filesStorageDir.isDirectory() && inputDiff.isFile()) {
            LOGGER.logDebug(
                    "Executing external git command on working directory: " + this.filesStorageDir.getAbsolutePath(),
                    "git apply --no-index --ignore-space-change --ignore-whitespace " + inputDiff.getAbsolutePath());
            ProcessBuilder processBuilder = new ProcessBuilder("git", "apply", "--no-index", "--ignore-space-change",
                    "--ignore-whitespace", inputDiff.getAbsolutePath());
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
        boolean success = false;

        if (filesStorageDir.isDirectory() && inputDiff.isFile()) {
            LOGGER.logDebug(
                    "Executing external git command on working directory: " + this.filesStorageDir.getAbsolutePath(),
                    "git apply --no-index --reverse " + inputDiff.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder("git", "apply", "--no-index", "--reverse",
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
