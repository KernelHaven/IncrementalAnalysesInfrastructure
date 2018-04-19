package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

/**
 * This is an {@link InputFilter} used to generate a set of
 * files containing only those files matching the fileRegex which were changed.
 */
public class ChangedOnlyFilter extends InputFilter {


	public ChangedOnlyFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex) throws IOException {
		super(sourceDirectory, diffFile, fileRegex);
	}


	@Override
	protected Collection<Path> doFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex) throws IOException {
	
		Collection<Path> paths = new ArrayList<Path>();
		for (FileEntry entry : diffFile.getEntries()) {
			if (entry.getType().equals(FileEntry.Type.ADDITION)
					|| entry.getType().equals(FileEntry.Type.MODIFICATION)) {
				paths.add(entry.getPath());
			}
		}
		return filterPathsByRegex(paths, fileRegex);
	}

}
