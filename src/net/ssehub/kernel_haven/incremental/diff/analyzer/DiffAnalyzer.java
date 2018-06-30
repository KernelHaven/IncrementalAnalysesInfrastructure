package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.incremental.diff.DiffFile;

/**
 * Abstract Analyzer class that can create a {@link FileEntry}-collection. Each
 * element of the resulting collection represents changes occurring in one file.
 * 
 * @author moritz
 * 
 */
public abstract class DiffAnalyzer {

    /**
     * Instantiates a new diff analyzer.
     */
    public DiffAnalyzer() {

    }

    /**
     * Parses the input given to the {@link DiffAnalyzer} and creates a
     * {@link DiffFile}.
     *
     * @param file
     *            the file
     * @return the diff file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract DiffFile generateDiffFile(File file) throws IOException;

}
