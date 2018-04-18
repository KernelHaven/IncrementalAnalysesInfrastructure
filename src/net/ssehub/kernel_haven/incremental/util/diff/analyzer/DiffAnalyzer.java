package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.IOException;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

/**
 * Abstract Analyzer class that can create a {@link FileEntry}-collection.
 * Each element of the resulting collection represents changes occuring in one file.
 * 
 * @author moritz floeter
 * 
 */
public abstract class DiffAnalyzer {


	/**
	 * Parses the input given to the DiffAnalyzer and creates a collection of FileEntries.
	 *
	 * @return the collection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract Collection<FileEntry> parse() throws IOException;

}
