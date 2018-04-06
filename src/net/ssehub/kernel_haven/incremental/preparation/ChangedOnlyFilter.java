package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;

/**
 * The Class ChangedOnlyFilter. This is an InputFilter used to generate a set of files containing only 
 * those files matching the fileRegex which were changed.
 */
public class ChangedOnlyFilter extends InputFilter {

	/**
	 * Instantiates a new changed only filter.
	 *
	 * @param sourceDirectory the source directory
	 * @param diffFile the diff file
	 * @param fileRegex the file regex
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ChangedOnlyFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {
		super(sourceDirectory, diffFile, fileRegex);
	}

	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.incremental.preparation.InputFilter#doFilter(java.io.File, java.io.File, java.util.regex.Pattern)
	 */
	@Override
	protected Collection<Path> doFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {
		DiffFile diffReader = new DiffFile(diffFile);
		Collection<Path> paths = diffReader.getModified();
		paths.addAll(diffReader.getAdded());
		return filterPathsByRegex(paths, fileRegex);
	}

}
