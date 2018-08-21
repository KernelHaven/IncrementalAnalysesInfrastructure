package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.FileChange;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Lines;
import net.ssehub.kernel_haven.util.Logger;

/**
 * The Class FileReplacingDiffApplier.
 */
public class FileReplacingDiffApplier extends DiffApplier {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.get();

    /** The files storage dir. */
    private final File filesStorageDir;

    /** The input diff. */
    private final DiffFile diffFile;

    /**
     * Instantiates a new {@link FileReplacingDiffApplier}.
     *
     * @param filesStorageDir
     *            the files storage dir
     * @param diffFile
     *            the diff file
     */
    public FileReplacingDiffApplier(File filesStorageDir, DiffFile diffFile) {
        this.filesStorageDir = filesStorageDir;
        this.diffFile = diffFile;
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        DiffFile diffFile = DiffFileParser.parse(new File(args[0]));
        DiffApplier applier =
            new FileReplacingDiffApplier(new File(args[1]), diffFile);
        applier.mergeChanges();
    }

    /**
     * Check preconditions for applying the changes described by the git diff
     * file.
     * 
     * @return true, if successful
     */
    private boolean checkMergePreconditions() {
        boolean preconditionsMet = true;

        // First handle deleted paths. Check if files that are to be deleted
        // exist.
        Set<Path> deletedPaths = new HashSet<Path>();
        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileChange.DELETION)) {
                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile()
                    .exists()) {
                    LOGGER.logError("File " + entry.getPath()
                        + " does not exist on filesystem eventhough the git-diff "
                        + "file used has a modification/deletion entry for it.");
                    preconditionsMet = false;
                }
                // make a list of deleted paths
                deletedPaths.add(entry.getPath());
            }
        }

        // Now handle modifications and additions
        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileChange.MODIFICATION)) {
                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile()
                    .exists()) {
                    LOGGER.logError("File " + entry.getPath()
                        + " does not exist on filesystem eventhough the git-diff "
                        + "file used has a modification/deletion entry for it.");
                    preconditionsMet = false;
                }

            } else if (entry.getType().equals(FileChange.ADDITION)) {
                // if the file exists and has not been identified as deleted
                // in the same diff file, this constitutes a conflict.
                if (filesStorageDir.toPath().resolve(entry.getPath()).toFile()
                    .exists() && !deletedPaths.contains(entry.getPath())) {
                    LOGGER.logError("File " + entry.getPath()
                        + " does already exist on filesystem eventhough the "
                        + "git-diff file used has an addition entry for it.");
                    preconditionsMet = false;
                }
            }
        }
        return preconditionsMet;
    }

    /**
     * Check preconditions for reverting the changes described by the git diff
     * file.
     *
     * @return true, if successful
     */
    private boolean checkRevertPreconditions() {
        boolean preconditionsMet = true;

        Set<Path> addedPaths = new HashSet<Path>();
        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileChange.ADDITION)) {
                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile()
                    .exists()) {
                    LOGGER.logError("File " + entry.getPath()
                        + " does not exist on filesystem eventhough the git-diff "
                        + "file used has a modification/addition entry for it.");
                    preconditionsMet = false;
                }
                addedPaths.add(entry.getPath());
            }

        }

        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileChange.MODIFICATION)) {
                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile()
                    .exists()) {
                    LOGGER.logError("File " + entry.getPath()
                        + " does not exist on filesystem eventhough the git-diff "
                        + "file used has a modification/addition entry for it.");
                    preconditionsMet = false;
                }
            } else if (entry.getType().equals(FileChange.DELETION)) {
                // if the file exists and has not been identified as added
                // in the same diff file, this constitutes a conflict.
                if (filesStorageDir.toPath().resolve(entry.getPath()).toFile()
                    .exists() && !addedPaths.contains(entry.getPath())) {
                    LOGGER.logError("File " + entry.getPath()
                        + " does already exist on filesystem eventhough the git-diff "
                        + "file used has an deletion entry for it.");
                    preconditionsMet = false;
                }
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
            try {
                // First handle deletions as sometimes files are described as
                // deleted and then added through a diff file
                for (FileEntry entry : diffFile.getEntries()) {
                    if (entry.getType().equals(FileEntry.FileChange.DELETION)) {
                        File fileInStorageDir = filesStorageDir.toPath()
                            .resolve(entry.getPath()).toFile();
                        fileInStorageDir.delete();
                    }
                }

                for (FileEntry entry : diffFile.getEntries()) {
                    Path filePath = entry.getPath();
                    File fileInStorageDir =
                        filesStorageDir.toPath().resolve(filePath).toFile();
                    if (entry.getType().equals(FileEntry.FileChange.ADDITION)
                        || entry.getType()
                            .equals(FileEntry.FileChange.MODIFICATION)) {
                        // delete old file
                        if (entry.getType()
                            .equals(FileEntry.FileChange.MODIFICATION)
                            && fileInStorageDir.exists()) {
                            fileInStorageDir.delete();
                        }
                        if (fileInStorageDir.getParentFile() != null) {
                            fileInStorageDir.getParentFile().mkdirs();
                        }
                        fileInStorageDir.createNewFile();
                        BufferedWriter writer = new BufferedWriter(
                            new FileWriter(fileInStorageDir));

                        // write new file
                        boolean firstLine = true;
                        for (Lines lines : entry.getLines()) {
                            if (lines.getType().equals(Lines.LineType.ADDED)
                                || lines.getType()
                                    .equals(Lines.LineType.UNMODIFIED)) {
                                if (!firstLine) {
                                    writer.write("\n");
                                } else {
                                    firstLine = false;
                                }
                                writer.write(lines.getContent());
                            }
                        }
                        writer.close();
                    }
                }
            } catch (IOException exc) {
                success = false;
                LOGGER.logException("Could not merge files ", exc);
            }
        } else {
            success = false;
        }
        if (success) {
            LOGGER.logInfo("Applied changes described by git-diff file.");
        } else {
            LOGGER
                .logError("Failed to apply changes described by git-diff file");
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
            try {
                // First handle additions as sometimes files are described as
                // deleted and then added through a diff file. Therefore
                // to revert, we first have to delete the added file and then
                // restore the deleted file.
                for (FileEntry entry : diffFile.getEntries()) {
                    if (entry.getType().equals(FileEntry.FileChange.ADDITION)) {
                        File fileInStorageDir = filesStorageDir.toPath()
                            .resolve(entry.getPath()).toFile();
                        fileInStorageDir.delete();
                    }
                }
                for (FileEntry entry : diffFile.getEntries()) {
                    Path filePath = entry.getPath();
                    File fileInStorageDir =
                        filesStorageDir.toPath().resolve(filePath).toFile();
                    if (entry.getType().equals(FileEntry.FileChange.DELETION)
                        || entry.getType()
                            .equals(FileEntry.FileChange.MODIFICATION)) {
                        // delete old file
                        if (entry.getType()
                            .equals(FileEntry.FileChange.MODIFICATION)
                            && fileInStorageDir.exists()) {
                            fileInStorageDir.delete();
                        }
                        if (fileInStorageDir.getParentFile() != null) {
                            fileInStorageDir.getParentFile().mkdirs();
                        }
                        fileInStorageDir.createNewFile();

                        BufferedWriter writer = new BufferedWriter(
                            new FileWriter(fileInStorageDir));

                        boolean firstLine = true;
                        for (Lines lines : entry.getLines()) {
                            if (lines.getType().equals(Lines.LineType.DELETED)
                                || lines.getType()
                                    .equals(Lines.LineType.UNMODIFIED)) {
                                if (!firstLine) {
                                    writer.write("\n");
                                } else {
                                    firstLine = false;
                                }
                                writer.write(lines.getContent());
                            }
                        }
                        writer.close();
                    }
                }
            } catch (IOException exc) {
                success = false;
                LOGGER.logException("Could not revert merge ", exc);
            }
        } else {
            success = false;
        }

        if (success) {
            LOGGER.logInfo("Reverted changes described by git-diff file.");
        } else {
            LOGGER.logError(
                "Failed to revert changes described by git-diff file.");
        }
        return success;
    }

}
