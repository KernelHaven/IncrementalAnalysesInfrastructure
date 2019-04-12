package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.FileChange;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Lines;
import net.ssehub.kernel_haven.util.Logger;

/**
 * A {@link DiffApplier} that assumes DiffFile-objects to contain the entire
 * context of the file (eg. diff files generated through git diff --no-renames
 * -U100000 oldCommitHash newCommitHash). It works by replacing existing files
 * with new files that consist of the information within the DiffFile object
 * exclusively (= it does not use information from the previous files in the
 * directory where the changes get applied). It ignores binary files and skips
 * them when applying the changes to a directory.
 * 
 * In contrast to the legacy {@link GitDiffApplier} git does not need to be
 * installed on the system.
 * 
 */
public class FileReplacingDiffApplier implements DiffApplier {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.get();

    /** The files storage dir. */
    private final File filesStorageDir;

    /** The input diff. */
    private final DiffFile diffFile;

    /**
     * Instantiates a new {@link DiffApplyUtil}.
     *
     * @param filesStorageDir the files storage dir
     * @param diffFile        the diff file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public FileReplacingDiffApplier(File filesStorageDir, DiffFile diffFile) {
        this.filesStorageDir = filesStorageDir;
        this.diffFile = diffFile;
    }

    /**
     * Check preconditions for applying the changes described by the git diff file.
     * 
     * @return true, if successful
     */
    private boolean checkMergePreconditions() {
        boolean preconditionsMet = true;

        // iterate over all entries within the diff file
        for (FileEntry entry : diffFile.getEntries()) {
            for (Lines lines : entry.getLines()) {
                if (lines.getType().equals(Lines.LineType.BETWEEN_CHUNKS) && lines.getCount() > 0) {
                    LOGGER.logError("The diff does not explicitly contain all lines of the files that it describes."
                            + " Use \"git diff --no-renames -U100000\" for generating the diff files "
                            + "to make sure that the diff file describes the changed files completely.");
                    LOGGER.logError("This concerns the file: " + entry.getPath().toString());
                    preconditionsMet = false;
                }
            }

            // only consider deletions and modifications. deleted and modified
            // files should be present before applying the diff.
            // We do not need to check for added files as those are not on
            // the filesystem before the diff is applied.
            if (entry.getType().equals(FileChange.DELETION) || entry.getType().equals(FileChange.MODIFICATION)) {

                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                    LOGGER.logError("File " + entry.getPath() + " does not exist on filesystem eventhough the"
                            + " git-diff file used has a modification/deletion entry for it.");
                    preconditionsMet = false;
                }

            } else if (entry.getType().equals(FileChange.ADDITION)
                    && filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                LOGGER.logError("File " + entry.getPath() + " does already exist on filesystem eventhough the"
                        + " git-diff file used has an addition entry for it.");
                preconditionsMet = false;

            }
        }
        return preconditionsMet;
    }

    /**
     * Check preconditions for reverting the changes described by the git diff file.
     *
     * @return true, if successful
     */
    private boolean checkRevertPreconditions() {
        boolean preconditionsMet = true;

        for (FileEntry entry : diffFile.getEntries()) {
            for (Lines lines : entry.getLines()) {
                if (lines.getType().equals(Lines.LineType.BETWEEN_CHUNKS) && lines.getCount() > 0) {
                    LOGGER.logError("The diff does not explicitly contain all lines of the files that it describes."
                            + " Use \"git diff --no-renames -U100000\" for generating the diff files "
                            + "to make sure that the diff file describes the changed files completely.");
                    LOGGER.logError("This concerns the file: " + entry.getPath().toString());
                    preconditionsMet = false;
                }
            }
            if (entry.getType().equals(FileChange.ADDITION) || entry.getType().equals(FileChange.MODIFICATION)) {

                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                    LOGGER.logError("File " + entry.getPath() + " does not exist on filesystem eventhough"
                            + " the git-diff file used has a modification/addition entry for it.");
                    preconditionsMet = false;
                }

            } else if (entry.getType().equals(FileChange.DELETION)
                    && filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                LOGGER.logError("File " + entry.getPath() + " does already exist on filesystem eventhough"
                        + " the git-diff file used has an deletion entry for it.");
                preconditionsMet = false;

            }
        }
        return preconditionsMet;
    }

    /**
     * Merge changes.
     *
     * @return true, if successful
     */
    public boolean mergeChanges() {
        LOGGER.logInfo("Applying changes described by git-diff file ... ");
        boolean success = true;
        if (checkMergePreconditions()) {
            BufferedWriter writer = null;
            try {
                for (FileEntry entry : diffFile.getEntries()) {
                    Path filePath = entry.getPath();
                    File fileInStorageDir = filesStorageDir.toPath().resolve(filePath).toFile();
                    if (entry.getType().equals(FileEntry.FileChange.ADDITION)
                            || entry.getType().equals(FileEntry.FileChange.MODIFICATION)) {
                        // delete old file
                        if (entry.getType().equals(FileEntry.FileChange.MODIFICATION) && fileInStorageDir.exists()) {
                            fileInStorageDir.delete();
                        }
                        if (fileInStorageDir.getParentFile() != null) {
                            fileInStorageDir.getParentFile().mkdirs();
                        }
                        fileInStorageDir.createNewFile();
                        writer = new BufferedWriter(new FileWriter(fileInStorageDir));

                        List<Lines> listOfLines = entry.getLines();
                        for (int i = 0; i < listOfLines.size(); i++) {
                            Lines lines = listOfLines.get(i);
                            if (lines.getType().equals(Lines.LineType.ADDED)
                                    || lines.getType().equals(Lines.LineType.UNMODIFIED) && lines.getCount() > 0) {
                                writer.write(lines.getContent());
                                if (i == listOfLines.size() - 1 && entry.hasNoNewLineAtEndOfFile()) {
                                    LOGGER.logWarning("No new line at end of file: " + entry.getPath());
                                } else {
                                    writer.write("\n");
                                }
                            }
                        }
                        writer.close();
                        Files.setPosixFilePermissions(fileInStorageDir.toPath(), entry.getPermissions());
                    } else if (entry.getType().equals(FileEntry.FileChange.DELETION)) {
                        fileInStorageDir.delete();
                    }
                }
            } catch (IOException exc) {
                success = false;
                LOGGER.logException("Could not merge files ", exc);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    // Nothing to be done here
                }
            }
        } else {
            success = false;
        }
        if (success) {
            LOGGER.logInfo("Applied changes described by git-diff file.");
        } else {
            LOGGER.logError("Failed to apply changes described by git-diff file");
        }

        return success;
    }

    /**
     * Revert changes.
     *
     * @return true, if successful
     */
    public boolean revertChanges() {
        boolean success = true;
        LOGGER.logInfo("Reverting changes described by git-diff file ... ");
        if (checkRevertPreconditions()) {
            BufferedWriter writer = null;
            try {
                for (FileEntry entry : diffFile.getEntries()) {
                    LOGGER.logDebug("Reverting changes for file entry: " + entry.getPath());
                    Path filePath = entry.getPath();
                    File fileInStorageDir = filesStorageDir.toPath().resolve(filePath).toFile();
                    if (entry.getType().equals(FileEntry.FileChange.DELETION)
                            || entry.getType().equals(FileEntry.FileChange.MODIFICATION)) {
                        // delete old file
                        if (entry.getType().equals(FileEntry.FileChange.MODIFICATION) && fileInStorageDir.exists()) {
                            fileInStorageDir.delete();
                        }

                        if (fileInStorageDir.getParentFile() != null) {
                            fileInStorageDir.getParentFile().mkdirs();
                        }

                        fileInStorageDir.createNewFile();

                        writer = new BufferedWriter(new FileWriter(fileInStorageDir));

                        List<Lines> listOfLines = entry.getLines();
                        for (int i = 0; i < listOfLines.size(); i++) {
                            Lines lines = listOfLines.get(i);
                            if (lines.getType().equals(Lines.LineType.DELETED)
                                    || lines.getType().equals(Lines.LineType.UNMODIFIED) && lines.getCount() > 0) {
                                writer.write(lines.getContent());
                                if (i == listOfLines.size() - 1 && entry.hasNoNewLineAtEndOfFile()) {
                                    LOGGER.logWarning("No new line at end of file: " + entry.getPath());
                                } else {
                                    writer.write("\n");
                                }
                            }
                        }

                        writer.close();
                        // Set POSIX permissions
                        Files.setPosixFilePermissions(fileInStorageDir.toPath(), entry.getPermissions());
                    } else if (entry.getType().equals(FileEntry.FileChange.ADDITION)) {
                        fileInStorageDir.delete();
                    }
                }
            } catch (IOException exc) {
                success = false;
                LOGGER.logException("Could not revert merge ", exc);
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    // Nothing to be done here
                }
            }
        } else {
            success = false;
        }

        if (success) {
            LOGGER.logInfo("Reverted changes described by git-diff file.");
        } else {
            LOGGER.logError("Failed to revert changes described by git-diff file.");
        }
        return success;
    }

}
