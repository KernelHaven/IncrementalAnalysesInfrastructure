package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class InputFilter {

	protected Collection<Path> result = null;

	public InputFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {
		this.result = this.doFilter(sourceDirectory, diffFile, fileRegex);
	}
	
	protected Collection<Path> filterPathsByRegex(Collection<Path> unfilteredPaths, Pattern regex) {
		Collection<Path> filteredPaths = new ArrayList<Path>();
		for (Path path : unfilteredPaths) {
			Matcher m = regex.matcher(path.toString());
			if (m.matches()) {
				filteredPaths.add(path);
			}
		}
		return filteredPaths;
	}

	protected abstract Collection<Path> doFilter(File sourceDirectory, File diffFile, Pattern fileRegex)
			throws IOException;

	public Collection<Path> getFilteredResult() {
		return this.result;
	}

}
