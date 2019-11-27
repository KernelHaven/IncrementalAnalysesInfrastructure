package net.ssehub.kernel_haven.incremental.diff.linecount;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Lines;

/**
 * The Class LineCounter can be used to calculate the resulting position of a
 * line after applying changes described through a {@link DiffFile}.
 * 
 * @author moritz
 */
public class LineCounter {

    /** The diff file. */
    private DiffFile diffFile;

    /**
     * Instantiates a new line counter.
     *
     * @param diffFile the git diff file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public LineCounter(DiffFile diffFile) throws IOException {
        this.diffFile = diffFile;

    }

    /**
     * Gets the new line number.
     *
     * @param file           the file
     * @param numberToAdjust the number to adjust
     * @return the new line number
     */
    public int getNewLineNumber(Path file, int numberToAdjust) {
        int adjustedLineNumber = 0;

        int positionInOriginalFile = 0;
        int positionInNewFile = 0;

        List<Lines> chunks = diffFile.getEntry(file).getLines();

        boolean overstepped = false;
        Lines lastChunk = null;

        // Determine the position before a chunk that oversteps the position that is to
        // be adjusted. The result are the respective positions for both the old and new
        // file.
        for (int i = 0; i < chunks.size() && !overstepped; i++) {
            Lines currentChunk = chunks.get(i);
            
            if (currentChunk.getType().equals(Lines.LineType.ADDED)) {
                positionInNewFile += currentChunk.getCount();
            } else if (currentChunk.getType().equals(Lines.LineType.BETWEEN_CHUNKS)
                    || currentChunk.getType().equals(Lines.LineType.UNMODIFIED)) {
                positionInNewFile += currentChunk.getCount();
                positionInOriginalFile += currentChunk.getCount();
            } else if (currentChunk.getType().equals(Lines.LineType.DELETED)) {
                positionInOriginalFile += currentChunk.getCount();
            }

            // If we either overstep or reach the exact line that needs to be adjusted, we can stop.
            if (positionInOriginalFile > numberToAdjust) {
                overstepped = true;
            } else if (positionInOriginalFile == numberToAdjust) {
                break;
            }
                    
            lastChunk = currentChunk;
        }

        // If we did overstepped, we need to remove the last step we took
        if (overstepped) {
            lastChunk = notNull(lastChunk); // lastChunk is not null if overstepped == true
            if (lastChunk.getType().equals(Lines.LineType.UNMODIFIED)
                    || lastChunk.getType().equals(Lines.LineType.BETWEEN_CHUNKS)) {
                positionInOriginalFile = positionInOriginalFile - lastChunk.getCount();
                positionInNewFile = positionInNewFile - lastChunk.getCount();
            } else if (lastChunk.getType().equals(Lines.LineType.DELETED)) {
                positionInOriginalFile = positionInOriginalFile - lastChunk.getCount();
            }
            
        }

        // This leaves us with two positions which represent the positions where the
        // chunk starts
        // in which we assume our line with the numberToAdjust to be.

        // We substract the position in the original file from the number that is to be
        // adjusted
        // in order to get the distance from the last chunk.
        // As positionInNewFile represents the end of that chunk in the new file, we add
        // the difference to that position.
        adjustedLineNumber = numberToAdjust - positionInOriginalFile + positionInNewFile;

        return adjustedLineNumber;
    }

}