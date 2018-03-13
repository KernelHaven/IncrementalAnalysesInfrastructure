package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.util.DiffFile;

public class ChangedOnlyFilter extends InputFilter {

	public ChangedOnlyFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {
		super(sourceDirectory, diffFile, fileRegex);
	}

	@Override
	protected Collection<Path> doFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {
		DiffFile diffReader = new DiffFile(diffFile);
		Collection<Path> paths = diffReader.getModifiedOrUpdated();
		return filterPathsByRegex(paths, fileRegex);
	}

}
