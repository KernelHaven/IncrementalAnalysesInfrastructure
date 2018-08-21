package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;

/**
 * A simple {@link DiffAnalyzer}-Implementation that only analyzes the type of
 * change (that is Addition, Deletion or Modification) for each file. Use
 * {@link SimpleDiffAnalyzer} if you only need this information.
 * {@link VariabilityDiffAnalyzer} will also analyze for variability-changes
 * within the file-change but will take up more resources than
 * {@link SimpleDiffAnalyzer} for the task.
 * 
 * @author moritz
 * 
 */
public class SimpleDiffAnalyzer extends DiffAnalyzer {

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse
     * ()
     */
    @Override
    public DiffFile generateDiffFile(File file) throws IOException {

        return DiffFileParser.parse(file);
    }

}
