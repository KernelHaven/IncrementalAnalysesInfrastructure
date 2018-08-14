package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Lines;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Type;
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
     * Instantiates a new {@link DiffApplyUtil}.
     *
     * @param filesStorageDir
     *            the files storage dir
     * @param diffFile
     *            the diff file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public FileReplacingDiffApplier(File filesStorageDir, DiffFile diffFile)
        throws IOException {
        this.filesStorageDir = filesStorageDir;
        this.diffFile = diffFile;
    }

    /**
     * Instantiates a new file replacing diff applier.
     *
     * @param filesStorageDir
     *            the files storage dir
     * @param diffFile
     *            the diff file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public FileReplacingDiffApplier(File filesStorageDir, File diffFile) {
        this.filesStorageDir = filesStorageDir;
        LOGGER.logInfo("Initializing " + this.getClass().getSimpleName()
            + ". Parsing git diff file.");
        this.diffFile = new DiffFileParser().parse(diffFile);
        LOGGER.logInfo("Initialization of  " + this.getClass().getSimpleName()
            + " finished.");
    }

    /**
     * Check preconditions for applying the changes described by the git diff
     * file.
     * 
     * @return true, if successful
     */
    private boolean checkMergePreconditions() {
        boolean preconditionsMet = true;

        // iterate over all entries within the diff file
        for (FileEntry entry : diffFile.getEntries()) {
            // only consider deletions and modifications. deleted and modified
            // files should be present before applying the diff.
            // We do not need to check for added files as those are not on
            // the filesystem before the diff is applied.
            if (entry.getType().equals(Type.DELETION)
                || entry.getType().equals(Type.MODIFICATION)) {
      
                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                    LOGGER.logError("File " + entry.getPath() + " does not exist on filesystem eventhough the git-diff file used has a modification/deletion entry for it.");
                    preconditionsMet = false;
                }

            } else if (entry.getType().equals(Type.ADDITION)){
                if (filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                    LOGGER.logError("File " + entry.getPath() + " does already exist on filesystem eventhough the git-diff file used has an addition entry for it.");
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

        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(Type.ADDITION)
                || entry.getType().equals(Type.MODIFICATION)) {
      
                if (!filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                    LOGGER.logError("File " + entry.getPath() + " does not exist on filesystem eventhough the git-diff file used has a modification/addition entry for it.");
                    preconditionsMet = false;
                }

            } else if (entry.getType().equals(Type.DELETION)){
                if (filesStorageDir.toPath().resolve(entry.getPath()).toFile().exists()) {
                    LOGGER.logError("File " + entry.getPath() + " does already exist on filesystem eventhough the git-diff file used has an deletion entry for it.");
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
                for (FileEntry entry : diffFile.getEntries()) {

                    Path filePath = entry.getPath();
                    File fileInStorageDir =
                        filesStorageDir.toPath().resolve(filePath).toFile();
                    if (entry.getType().equals(FileEntry.Type.ADDITION) || entry
                        .getType().equals(FileEntry.Type.MODIFICATION)) {
                        // delete old file
                        if (entry.getType().equals(FileEntry.Type.MODIFICATION)
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
                        for (Lines lines : entry.getLines()) {
                            if (lines.getType().equals(Lines.LineType.ADDED)
                                || lines.getType()
                                    .equals(Lines.LineType.UNMODIFIED)) {
                                writer.write(lines.getContent());
                            }
                        }
                        writer.close();
                    } else if (entry.getType()
                        .equals(FileEntry.Type.DELETION)) {
                        fileInStorageDir.delete();
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
                for (FileEntry entry : diffFile.getEntries()) {
                    Path filePath = entry.getPath();
                    File fileInStorageDir =
                        filesStorageDir.toPath().resolve(filePath).toFile();
                    if (entry.getType().equals(FileEntry.Type.DELETION) || entry
                        .getType().equals(FileEntry.Type.MODIFICATION)) {
                        // delete old file
                        if (entry.getType().equals(FileEntry.Type.MODIFICATION)
                            && fileInStorageDir.exists()) {
                            fileInStorageDir.delete();
                        }

                        if (fileInStorageDir.getParentFile() != null) {
                            fileInStorageDir.getParentFile().mkdirs();
                        }
                        fileInStorageDir.createNewFile();

                        BufferedWriter writer = new BufferedWriter(
                            new FileWriter(fileInStorageDir));

                        for (Lines lines : entry.getLines()) {
                            if (lines.getType().equals(Lines.LineType.DELETED)
                                || lines.getType()
                                    .equals(Lines.LineType.UNMODIFIED)) {
                                writer.write(lines.getContent());
                            }
                        }
                        writer.close();
                    } else if (entry.getType()
                        .equals(FileEntry.Type.ADDITION)) {
                        fileInStorageDir.delete();
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
