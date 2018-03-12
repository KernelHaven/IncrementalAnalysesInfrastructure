package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.DiffFileReader;

public class ChangedOnlyFilter extends InputFilter {

	public ChangedOnlyFilter(File sourceDirectory, File diffFile) throws IOException {
		super(sourceDirectory, diffFile);
	}

	@Override
	protected Collection<Path> doFilter(File sourceDirectory, File diffFile) throws IOException {

		DiffFileReader diffReader = new DiffFileReader(diffFile);
		return diffReader.getModifiedOrUpdated();
	}

}
