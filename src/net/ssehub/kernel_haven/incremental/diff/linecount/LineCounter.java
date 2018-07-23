package net.ssehub.kernel_haven.incremental.diff.linecount;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.diff.linecount.LineInfoExtractor.Lines;

// TODO: Auto-generated Javadoc
/**
 * The Class LineCounter.
 * @author moritz
 */
public class LineCounter {

    /** The extractor. */
    private LineInfoExtractor extractor;

    /**
     * Instantiates a new line counter.
     *
     * @param gitDiffFile            the git diff file
     * @param ignorePaths            list of paths to ignore
     * @param fileInclusionRegex defines which files to include
     * @throws IOException             Signals that an I/O exception has occurred.
     */
    public LineCounter(File gitDiffFile, Collection<Path> ignorePaths, Pattern fileInclusionRegex)
        throws IOException {
        extractor = new LineInfoExtractor(gitDiffFile, ignorePaths, fileInclusionRegex);
    }

    /**
     * Gets the new line number.
     *
     * @param file
     *            the file
     * @param originalLineNumber
     *            the original line number
     * @return the new line number
     */
    public int getNewLineNumber(Path file, int originalLineNumber) {
        int newLineNumber = 0;

        int positionInOriginalFile = 0;
        int positionInNewFile = 0;
        List<Lines> chunks = extractor.getLines(file);

        for (int i = 0; positionInOriginalFile < originalLineNumber
            && i < chunks.size(); i++) {
            newLineNumber = positionInNewFile
                + (originalLineNumber - positionInOriginalFile);

            Lines lines = chunks.get(i);

            if (Lines.LineType.ADDED.equals(lines.getType())) {
                positionInNewFile += lines.getCount();
            } else if (Lines.LineType.DELETED.equals(lines.getType())) {
                positionInOriginalFile += lines.getCount();
            } else {
                positionInNewFile += lines.getCount();
                positionInOriginalFile += lines.getCount();
            }
        }
        return newLineNumber;
    }

}
