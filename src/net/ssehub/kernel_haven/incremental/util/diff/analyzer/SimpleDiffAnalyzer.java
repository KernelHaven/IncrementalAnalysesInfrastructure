package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

public class SimpleDiffAnalyzer extends DiffAnalyzer {

	private File file;

	public SimpleDiffAnalyzer(File file) {
		this.file = file;
	}

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
