package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import diff.DiffAnalyzer;
import net.ssehub.kernel_haven.incremental.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.FileEntry;
import net.ssehub.kernel_haven.util.Logger;

/**
 * The Class ChangedOnlyFilter. This is an {@link InputFilter} that can be used
 * to generate a collection containing changed files matching the regular
 * expression.
 * 
 * @author moritz
 */
public class VariabilityChangeFilter extends InputFilter {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.get();

    /**
     * Instantiates a new variability change filter.
     *
     * @param sourceDirectory
     *            the source directory
     * @param diffFile
     *            the diff file
     * @param fileRegex
     *            the regular expressions for files to include
     * @param includeDeletions
     *            defines whether or not to include deletions
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public VariabilityChangeFilter(File sourceDirectory, DiffFile diffFile,
        Pattern fileRegex, boolean includeDeletions) throws IOException {
        super(sourceDirectory, diffFile, fileRegex, includeDeletions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ssehub.kernel_haven.incremental.preparation.filter.InputFilter#
     * doFilter( java.io.File,
     * net.ssehub.kernel_haven.incremental.diff.DiffFile,
     * java.util.regex.Pattern, boolean)
     */
    @Override
    protected Collection<Path> doFilter(File sourceDirectory, DiffFile diffFile,
        Pattern fileRegex, boolean includeDeletions) throws IOException {
        Collection<Path> paths = new ArrayList<Path>();
        for (FileEntry entry : diffFile.getEntries()) {
            if (includeDeletions
                || entry.getType().equals(FileEntry.Type.ADDITION)
                || entry.getType().equals(FileEntry.Type.MODIFICATION)) {

                // include entries marked as change
                if (entry.getVariabilityChange()
                    .equals(FileEntry.VariabilityChange.CHANGE)) {
                    paths.add(entry.getPath());
                    // as a fallback for entries that were not analyzed also
                    // include those.
                } else if (entry.getVariabilityChange()
                    .equals(FileEntry.VariabilityChange.NOT_ANALYZED)) {
                    // This should only happen when the diff-file was analyzed
                    // incorrectly or an
                    // analyzer was used
                    // that did not analyze for variability
                    LOGGER.logError(
                        "The following FileEntry was not analyzed for variability-changes.\nPerhaps the "
                            + DiffAnalyzer.class.getSimpleName()
                            + " you used does not analyze for "
                            + "variability-changes.\nFallback: "
                            + "including file for extraction.\n" + entry);
                    paths.add(entry.getPath());
                }
            }
        }
        return filterPathsByRegex(paths, fileRegex);
    }

}
