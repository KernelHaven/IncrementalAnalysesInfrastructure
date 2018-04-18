package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

/**
 * A simple {@link DiffAnalyzer}-Implementation that only analyzes
 * the type of change (that is Addition, Deletion or Modification) for
 * each file. Use {@link SimpleDiffAnalyzer} if you only need this
 * information. {@link ComAnDiffAnalyzer} will also analyze for
 * variability-changes within the file-change but will take up
 * more resources than {@link SimpleDiffAnalyzer} for the task.
 */
public class SimpleDiffAnalyzer extends DiffAnalyzer {

	/** The file that is to be parsed. */
	private File file;

	/**
	 * Instantiates a new {@link SimpleDiffAnalyzer}.
	 * @param diffFile the file that is to be parsed. Should be in the git-diff format.
	 */
	public SimpleDiffAnalyzer(File diffFile) {
		this.file = diffFile;
	}

	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse()
	 */
	@Override
	public Collection<FileEntry> parse() throws IOException {
		Collection<FileEntry> changed = new ArrayList<FileEntry>();

		// We can not read lines (e.g. via Files.readAllLines(path)) to an array/list and iterate over it as this fails
		// for huge input-files such as the initial commit for a bigger software-project
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String currentLine = br.readLine();

			for (String nextLine; (nextLine = br.readLine()) != null;) {
				if (currentLine.startsWith("diff --git ")) {
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
				currentLine = nextLine;
			}
		}
		return changed;
	}

}
