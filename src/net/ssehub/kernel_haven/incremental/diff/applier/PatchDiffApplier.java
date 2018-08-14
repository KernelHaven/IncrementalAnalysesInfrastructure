package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.incremental.diff.analyzer.SimpleDiffAnalyzer;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Type;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

public class PatchDiffApplier extends DiffApplier {

    /** The files storage dir. */
    private final File filesStorageDir;

    /** The input diff. */
    private final File inputDiff;

    private final DiffFile diffFile;

    private static final Logger LOGGER = Logger.get();

    public boolean revertChanges() {
        boolean success = false;

        if (filesStorageDir.isDirectory() && inputDiff.isFile()) {
            LOGGER.logDebug(
                "Executing external patch command: "
                    + this.filesStorageDir.getAbsolutePath(),
                "patch -p1 -N -R --fuzz=0 --ignore-whitespace  -i "
                    + inputDiff.getAbsolutePath() + "-d"
                    + filesStorageDir.getAbsolutePath());
            ProcessBuilder processBuilder = new ProcessBuilder("patch", "-p1",
                "-N", "-R", "--fuzz=0", "--ignore-whitespace",
                "-i", inputDiff.getAbsolutePath(), "-d",
                filesStorageDir.getAbsolutePath());
            ProcessBuilder dryRunProcessBuilder = new ProcessBuilder("patch",
                "-p1", "-N", "-R", "--dry-run","--fuzz=0",
                "--ignore-whitespace", "-i", inputDiff.getAbsolutePath(), "-d",
                filesStorageDir.getAbsolutePath());

            ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

            try {
                if (Util.executeProcess(dryRunProcessBuilder, "patch -R --dry-run",
                    stdoutStream, stderrStream, 0)) {
                    stdoutStream = new ByteArrayOutputStream();
                    stderrStream = new ByteArrayOutputStream();
                    success = Util.executeProcess(processBuilder, "patch",
                        stdoutStream, stderrStream, 0);
                    if (success) {
                        for (FileEntry entry : this.diffFile.getEntries()) {
                            if (entry.getType().equals(Type.ADDITION)) {
                                File fileToDelete = filesStorageDir.toPath()
                                    .resolve(entry.getPath()).toFile();
                                if (fileToDelete.exists()) {
                                    fileToDelete.delete();
                                }
                            } else if (entry.getType().equals(Type.DELETION)) {
                                File fileToAdd = filesStorageDir.toPath()
                                    .resolve(entry.getPath()).toFile();
                                if (!fileToAdd.exists()) {
                                    fileToAdd.createNewFile();
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.logError("Could not revert changes");
                }
            } catch (IOException e) {
                LOGGER.logException("Could not revert changes", e);
            }

            String stderr = stderrStream.toString();
            String stdout = stdoutStream.toString();
            if (stderr != null && !stderr.equals("")) {
                if (!success) {
                    LOGGER
                        .logError(("patch stderr:\n" + stderr).split("\n"));
                } else {
                    LOGGER
                        .logInfo(("patch stderr:\n" + stderr).split("\n"));
                }
            }

            if ((stdout != null && !stdout.equals(""))) {
                LOGGER.logInfo(("patch stout:\n" + stdout).split("\n"));
            }

        }

        return success;
    }

    public PatchDiffApplier(File filesStorageDir, File inputDiff)
        throws IOException {
        this.filesStorageDir = filesStorageDir;
        this.inputDiff = inputDiff;
        this.diffFile = new SimpleDiffAnalyzer().generateDiffFile(inputDiff);
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
                "Executing external patch command: "
                    + "patch -p1 -N --fuzz=0 --ignore-whitespace -i "
                    + inputDiff.getAbsolutePath() + " -d "
                    + filesStorageDir.getAbsolutePath());

            ProcessBuilder dryRunProcessBuilder = new ProcessBuilder("patch",
                "-p1", "-N", "--dry-run", "--fuzz=0",
                "--ignore-whitespace", "-i", inputDiff.getAbsolutePath(), "-d",
                filesStorageDir.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder("patch", "-p1",
                "-N", "--fuzz=0", "--ignore-whitespace", "-i",
                inputDiff.getAbsolutePath(), "-d",
                filesStorageDir.getAbsolutePath());

            ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

            try {
                // Only execute on filesystem if dry run is successful
                if (Util.executeProcess(dryRunProcessBuilder,
                    "patch --dry-run", stdoutStream, stderrStream, 0)) {

                     stdoutStream = new ByteArrayOutputStream();
                     stderrStream = new ByteArrayOutputStream();
                    success = Util.executeProcess(processBuilder, "patch",
                        stdoutStream, stderrStream, 0);

                    // patch does not remove deleted files, so we do it here.
                    if (success) {
                        for (FileEntry entry : this.diffFile.getEntries()) {
                            if (entry.getType().equals(Type.DELETION)) {
                                File fileToDelete = filesStorageDir.toPath()
                                    .resolve(entry.getPath()).toFile();
                                if (fileToDelete.exists()) {
                                    fileToDelete.delete();
                                }
                                // Handle empty files aswell as patch does not
                                // cover them reliably
                            } else if (entry.getType().equals(Type.ADDITION)) {
                                File fileToAdd = filesStorageDir.toPath()
                                    .resolve(entry.getPath()).toFile();
                                if (!fileToAdd.exists()) {
                                    fileToAdd.createNewFile();
                                }
                            }
                        }
                    }
                } else {
                    LOGGER.logError("Could not merge changes");
                }
            } catch (IOException e) {
                LOGGER.logException("Could not merge changes", e);
            }

            String stderr = stderrStream.toString();
            String stdout = stdoutStream.toString();
            if (stderr != null && !stderr.equals("")) {
                if (!success) {
                    LOGGER
                        .logError(("patch stderr:\n" + stderr).split("\n"));
                } else {
                    LOGGER
                        .logInfo(("patch stderr:\n" + stderr).split("\n"));
                }
            }

            if ((stdout != null && !stdout.equals(""))) {
                LOGGER.logInfo(("patch stout:\n" + stdout).split("\n"));
            }

        }

        return success;
    }
}
