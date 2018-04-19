package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

/**
 * Abstract Analyzer class that can create a {@link FileEntry}-collection. Each
 * element of the resulting collection represents changes occuring in one file.
 * 
 * @author moritz floeter
 * 
 */
public interface DiffAnalyzer {

	/**
	 * Parses the input given to the {@link DiffAnalyzer} and creates a
	 * {@link DiffFile}.
	 *
	 * @return the diff file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static DiffFile generateDiffFile(File file) throws IOException {
		return null;

	}

}
