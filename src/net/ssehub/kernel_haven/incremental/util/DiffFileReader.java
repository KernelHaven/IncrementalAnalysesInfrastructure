package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import io.reflectoring.diffparser.api.DiffParser;
import io.reflectoring.diffparser.api.UnifiedDiffParser;
import io.reflectoring.diffparser.api.model.Diff;

public class DiffFileReader {

	private Collection<Diff> diff = null;

	public DiffFileReader(File file) throws FileNotFoundException {
		DiffParser parser = new UnifiedDiffParser();
		InputStream in = new FileInputStream(file);
		this.diff = parser.parse(in);
	}

	public Collection<Path> getModifiedOrUpdated() {
		Collection<Path> modifiedOrUpdated = new ArrayList<Path>();
		for (Diff diffEntry : diff) {
			if (diffEntry != null && !diffEntry.getToFileName().toLowerCase(Locale.ENGLISH).equals("/dev/null")) {
				// add and cut away the git-prefix -> "b/path/to/file" becomes "path/to/file"
				modifiedOrUpdated.add(Paths.get(diffEntry.getToFileName().substring(2)));
			}
		}
		return modifiedOrUpdated;
	}

	public Collection<Path> getDeleted() {
		Collection<Path> deleted = new ArrayList<Path>();
		for (Diff diffEntry : diff) {
			if (diffEntry != null && diffEntry.getToFileName().toLowerCase(Locale.ENGLISH).equals("/dev/null")) {
				// add and cut away the git-prefix -> "a/path/to/file" becomes "path/to/file"
				deleted.add(Paths.get(diffEntry.getFromFileName().substring(2)));
			}
		}
		return deleted;
	}

}
