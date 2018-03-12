package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public abstract class InputFilter {

	protected Collection<Path> result = null;

	public InputFilter(File sourceDirectory, File diffFile) throws IOException {
		this.result = this.doFilter(sourceDirectory, diffFile);
	}

	protected abstract Collection<Path> doFilter(File sourceDirectory, File diffFile) throws IOException;

	public Collection<Path> getFilteredResult() {
		return this.result;
	}

}
