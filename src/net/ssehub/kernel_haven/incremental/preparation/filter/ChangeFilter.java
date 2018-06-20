package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.FileEntry;

// TODO: Auto-generated Javadoc
/**
 * This is an {@link InputFilter} used to generate a set of
 * files containing only those files matching the fileRegex which were changed.
 */
public class ChangeFilter extends InputFilter {


	/**
	 * Instantiates a new change filter.
	 *
	 * @param sourceDirectory the source directory
	 * @param diffFile the diff file
	 * @param fileRegex the file regex
	 * @param includeDeletions the include deletions
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ChangeFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex, boolean includeDeletions) throws IOException {
		super(sourceDirectory, diffFile, fileRegex, includeDeletions);
	}


	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.incremental.preparation.filter.InputFilter#doFilter(java.io.File, net.ssehub.kernel_haven.incremental.diff.DiffFile, java.util.regex.Pattern, boolean)
	 */
	@Override
	protected Collection<Path> doFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex, boolean includeDeletions) throws IOException {
	
		Collection<Path> paths = new ArrayList<Path>();
		for (FileEntry entry : diffFile.getEntries()) {
			if (includeDeletions 
					|| entry.getType().equals(FileEntry.Type.ADDITION)
					|| entry.getType().equals(FileEntry.Type.MODIFICATION)) {
				paths.add(entry.getPath());
			}
		}
		return filterPathsByRegex(paths, fileRegex);
	}

}
