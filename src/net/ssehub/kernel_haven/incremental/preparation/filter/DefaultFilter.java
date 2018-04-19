package net.ssehub.kernel_haven.incremental.preparation.filter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;

/**
 * A filter which does only filter the regex-pattern.
 */
public class DefaultFilter extends InputFilter {


	public DefaultFilter(File sourceDirectory, DiffFile diffFile, Pattern fileRegex) throws IOException {
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

		Collection<File> files = FolderUtil.listRelativeFiles(sourceDirectory, true);
		Collection<Path> paths = new ArrayList<Path>();
		for (File file : files) {
			paths.add(file.toPath());
		}

		paths = filterPathsByRegex(paths, fileRegex);

		return paths;
	}
}
