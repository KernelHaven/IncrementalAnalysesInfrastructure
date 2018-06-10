package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import diff.DiffAnalyzer;
import net.ssehub.kernel_haven.incremental.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.FileEntry;
import net.ssehub.kernel_haven.util.Logger;

/**
 * The Class ChangedOnlyFilter. This is an InputFilter used to generate a set of
 * files containing only those files matching the fileRegex which were changed.
 */
public class VariabilityChangeFilter extends InputFilter {
	private static final Logger LOGGER = Logger.get();

	/**
	 * Instantiates a new changed only filter.
	 *
	 * @param sourceDirectory
	 *            the source directory
	 * @param diffFile
	 *            the diff file
	 * @param fileRegex
	 *            the file regex
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public VariabilityChangeFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex) throws IOException {
		super(sourceDirectory, diffFile, fileRegex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.ssehub.kernel_haven.incremental.preparation.InputFilter#doFilter(java.io.
	 * File, java.io.File, java.util.regex.Pattern)
	 */
	@Override
	protected Collection<Path> doFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex) throws IOException {
		Collection<Path> paths = new ArrayList<Path>();
		for (FileEntry entry : diffFile.getEntries()) {
			if (entry.getVariabilityChange().equals(FileEntry.VariabilityChange.CHANGE)) {
				paths.add(entry.getPath());
			}

			if (entry.getVariabilityChange().equals(FileEntry.VariabilityChange.NOT_ANALYZED)) {
				// This only happens when the VariabilityChangeFilter did not process the file
				// correctly
				LOGGER.logError("The following FileEntry was not analyzed for variability-changes.\nPerhaps the "
						+ DiffAnalyzer.class.getSimpleName()
						+ " you used does not analyze for variability-changes.\nFallback: including file for extraction.\n"
						+ entry);
				paths.add(entry.getPath());
			}
		}
		return filterPathsByRegex(paths, fileRegex);
	}

}
