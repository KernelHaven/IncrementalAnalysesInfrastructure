package net.ssehub.kernel_haven.incremental.diff.linecount;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
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
	 * @param gitDiffFile the git diff file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public LineCounter(File gitDiffFile) throws IOException {
		diffFile = DiffFileParser.parse(gitDiffFile);

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

		// List chunks of lines. A chunk represents a sequence where a
		// modification
		// of the same type was performed (Deletion, Addition etc.)

		List<Lines> chunks = diffFile.getEntry(file).getLines();

		// iterate over all chunks to add the number of lines up until
		// the targeted position is reached
		for (int i = 0; positionInOriginalFile <= numberToAdjust && i < chunks.size(); i++) {

			adjustedLineNumber = positionInNewFile + (numberToAdjust - positionInOriginalFile);

			Lines lines = chunks.get(i);

			// Added lines affect the position of the new file:
			// if the current position is 0 and 5 lines are added,
			// the new relative position is 5 while the relative position
			// in the original file remains 0 as it does not have those lines.
			if (Lines.LineType.ADDED.equals(lines.getType())) {
				positionInNewFile += lines.getCount();
				// Similar to ADDED, deleted lines are only present in the
				// original file and not in the new file.
			} else if (Lines.LineType.DELETED.equals(lines.getType())) {
				positionInOriginalFile += lines.getCount();
				// neutral lines are present in both files and therefore affect
				// both positions
			} else {
				positionInNewFile += lines.getCount();
				positionInOriginalFile += lines.getCount();
			}

		}

		return adjustedLineNumber;
	}

}