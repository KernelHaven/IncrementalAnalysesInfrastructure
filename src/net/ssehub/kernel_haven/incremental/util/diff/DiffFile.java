package net.ssehub.kernel_haven.incremental.util.diff;

import java.io.IOException;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * The Class DiffFile. This class extracts information out of a diff-file.
 * 
 * @author Moritz
 */
public class DiffFile {

	/** The diff. */
	Collection<FileEntry> changeSet;

	/**
	 * Instantiates a new diff file reader. The file passed to this constructor must
	 * be a git diff file.
	 *
	 * @param file
	 *            the git diff file
	 * @throws IOException
	 */
	public DiffFile(@NonNull DiffAnalyzer analyzer) throws IOException {
		this.changeSet = analyzer.parse();
	}


	public Collection<FileEntry> getEntries() {
		return changeSet;
	}

}
