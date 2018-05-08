package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;
import net.ssehub.kernel_haven.util.Logger;

/**
 * A simple {@link DiffAnalyzer}-Implementation that only analyzes
 * the type of change (that is Addition, Deletion or Modification) for
 * each file. Use {@link SimpleDiffAnalyzer} if you only need this
 * information. {@link VariabilityDiffAnalyzer} will also analyze for
 * variability-changes within the file-change but will take up
 * more resources than {@link SimpleDiffAnalyzer} for the task.
 * 
 * @author moritz
 * 
 */
public class SimpleDiffAnalyzer implements DiffAnalyzer {


	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse()
	 */
	public static DiffFile generateDiffFile(File file) throws IOException {
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
						if (filePath == null) {
							Logger.get().logDebug("Deletion with no filepath : ", currentLine, nextLine);
						} 
					} else {
						type = FileEntry.Type.MODIFICATION;
					}
					changed.add(new FileEntry(Paths.get(filePath), type));
				}
				currentLine = nextLine;
			}
		}
		return new DiffFile(changed);
	}

}
