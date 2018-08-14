package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;

/**
 * Filter class for files located in the source tree. Classes extending this
 * class filter the files according to their own criteria such as changed files
 * or files where variability did change.
 * 
 * @author moritz
 */
public abstract class InputFilter {

    /** The result. */
    protected Collection<Path> result = null;

    /**
     * Instantiates a new input filter.
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
    public InputFilter(File sourceDirectory, DiffFile diffFile,
        Pattern fileRegex, boolean includeDeletions) throws IOException {
        this.result = this.doFilter(sourceDirectory, diffFile, fileRegex,
            includeDeletions);
    }

    /**
     * Filter paths to input files using a regular expression matching the
     * file-path.
     *
     * @param unfilteredPaths
     *            the unfiltered paths
     * @param regex
     *            the regex
     * @return the collection
     */
    protected Collection<Path> filterPathsByRegex(
        Collection<Path> unfilteredPaths, Pattern regex) {
        Collection<Path> filteredPaths = new ArrayList<Path>();
        for (Path path : unfilteredPaths) {
            Matcher m = regex.matcher(path.toString());
            if (m.matches()) {
                filteredPaths.add(path);
            }
        }
        return filteredPaths;
    }

    /**
     * Extracts the relevant set of filepaths. Assumes sourceDirectory to be the
     * directory where the relvant files are stored and diffFile to describe
     * newly introduced changes between the current version of files in
     * sourceDirectory and previous version.
     *
     * @param sourceDirectory
     *            the source directory
     * @param diffFile
     *            the diff file
     * @param fileRegex
     *            the regular expression defining which file-paths are accepted
     * @param includeDeletions
     *            defines whether deletion entries are included aswell.
     * @return the collection
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected abstract Collection<Path> doFilter(File sourceDirectory,
        DiffFile diffFile, Pattern fileRegex, boolean includeDeletions)
        throws IOException;

    /**
     * Gets the filtered result.
     *
     * @return the filtered result
     */
    public Collection<Path> getFilteredResult() {
        return this.result;
    }

}
