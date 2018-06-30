package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.FileEntry;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;

/**
 * An implementation of {@link InputFilter} that can be used to generate a
 * collection of all files matching the regular expression.
 * 
 * @author moritz
 */
public class DefaultFilter extends InputFilter {

    /**
     * Instantiates a new default filter.
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
    public DefaultFilter(File sourceDirectory, DiffFile diffFile,
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

        Collection<File> files =
            FolderUtil.listRelativeFiles(sourceDirectory, true);
        Collection<Path> paths = new ArrayList<Path>();
        for (File file : files) {
            paths.add(file.toPath());
        }

        /*
         * including deletions for the {@link DefaultFilter} is currently not
         * technically necessary for the incremental infrastructure as a full
         * extraction will be performed anyways for every model. However the
         * functionality is implemented to correctly fulfill the expectations of
         * the includeDeletions option for the {@link DefaultFilter} as well.
         */
        if (includeDeletions) {
            diffFile.getEntries().stream()
                .filter(
                    entry -> entry.getType().equals(FileEntry.Type.DELETION))
                .forEach(entry -> paths.add(entry.getPath()));
        }

        return filterPathsByRegex(paths, fileRegex);
    }
}
