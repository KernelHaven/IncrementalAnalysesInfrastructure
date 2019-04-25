package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;

/**
 * This is an {@link InputFilter} that can be used to generate a collection
 * containing added files that matched the regular expression. Never includes
 * deletions, regardless of the boolean value passed to it.
 * 
 * @author moritz
 */
public class AdditionFilter extends InputFilter {

    /**
     * Instantiates a new change filter.
     *
     * @param sourceDirectory  the source directory
     * @param diffFile         the diff file
     * @param fileRegex        the file regex
     * @param includeDeletions the include deletions
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public AdditionFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex, boolean includeDeletions)
            throws IOException {
        super(sourceDirectory, diffFile, fileRegex, includeDeletions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ssehub.kernel_haven.incremental.preparation.filter.InputFilter#
     * doFilter( java.io.File, net.ssehub.kernel_haven.incremental.diff.DiffFile,
     * java.util.regex.Pattern, boolean)
     */
    @Override
    protected Collection<Path> doFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex,
            boolean includeDeletions) throws IOException {

        Collection<Path> paths = new ArrayList<>();
        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileEntry.FileChange.ADDITION)) {
                paths.add(entry.getPath());
            }
        }
        return filterPathsByRegex(paths, fileRegex);
    }

}
