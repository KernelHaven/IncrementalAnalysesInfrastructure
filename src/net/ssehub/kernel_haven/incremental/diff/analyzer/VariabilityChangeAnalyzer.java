package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.IOException;

import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;

/**
 * Abstract Analyzer class that can analyze changes for variability changes and
 * modifies the {@link DiffFile} object accordingly.
 * 
 * @author moritz
 * 
 */
public interface VariabilityChangeAnalyzer {

    /**
     * Parses the input given to the {@link VariabilityChangeAnalyzer} and creates a
     * {@link DiffFile}.
     *
     * @param diffFile the diff file
     * @param config   the config
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract void analyzeDiffFile(DiffFile diffFile, Configuration config) throws IOException;

}
