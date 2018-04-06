package net.ssehub.kernel_haven.incremental.util.diff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.ssehub.kernel_haven.incremental.util.diff.FileEntry.Type;

/**
 * The Class DiffFile. This class extracts information out of a diff-file.
 * 
 * @author Moritz
 */
public class DiffFile {

	/** The diff. */
	Collection<FileEntry> changeSet;

	/**
	 * Instantiates a new diff file reader. The file passed to this constructor must
	 * be a git diff file.
	 *
	 * @param file
	 *            the git diff file
	 * @throws IOException
	 */
	public DiffFile(File file) throws IOException {
		this.changeSet = parse(file);
	}

	private Collection<FileEntry> parse(File file) throws IOException {
		Collection<FileEntry> changed = new ArrayList<FileEntry>();
		List<String> lines = Files.readAllLines(file.toPath());
		for (int i = 0; i < lines.size(); i++) {
			String currentLine = lines.get(i);

			if (currentLine.startsWith("diff --git ")) {
				String nextLine = lines.get(i + 1);
				String filePath = currentLine.substring(currentLine.indexOf("a/") + "a/".length(),
						currentLine.indexOf(" b/"));
				FileEntry.Type type;
				if (nextLine.startsWith("new file mode")) {
					type = FileEntry.Type.ADDITION;
				} else if (nextLine.startsWith("deleted file mode")) {
					type = FileEntry.Type.DELETION;
				} else {
					type = FileEntry.Type.MODIFICATION;
				}
				changed.add(new FileEntry(Paths.get(filePath), type));
			}
		}
		return changed;
	}

	/**
	 * Gets paths to modified files.
	 *
	 * @return the modified or updated files
	 */
	public Collection<Path> getModified() {
		Collection<Path> modified = new ArrayList<Path>();
		for (FileEntry entry : this.changeSet) {
			if (entry.getType().equals(Type.MODIFICATION)) {
				modified.add(entry.getPath());
			}
		}
		return modified;
	}

	public Collection<Path> getAdded() {
		Collection<Path> added = new ArrayList<Path>();
		for (FileEntry entry : this.changeSet) {
			if (entry.getType().equals(Type.ADDITION)) {
				added.add(entry.getPath());
			}
		}
		return added;
	}

	/**
	 * Gets paths to deleted files.
	 *
	 * @return the deleted files
	 */
	public Collection<Path> getDeleted() {
		Collection<Path> deleted = new ArrayList<Path>();
		for (FileEntry entry : this.changeSet) {
			if (entry.getType().equals(Type.DELETION)) {
				deleted.add(entry.getPath());
			}
		}
		return deleted;
	}

}
