package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.util.FolderUtil;

public class BogusFilter extends InputFilter {

	public BogusFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {
		super(sourceDirectory, diffFile, fileRegex);
	}

	@Override
	protected Collection<Path> doFilter(File sourceDirectory, File diffFile, Pattern fileRegex) throws IOException {

		Collection<File> files = FolderUtil.listFiles(sourceDirectory, true);
		Collection<Path> paths = new ArrayList<Path>();
		for (File file : files) {
			paths.add(file.toPath());
		}

		paths = filterPathsByRegex(paths, fileRegex);

		return paths;
	}

}
