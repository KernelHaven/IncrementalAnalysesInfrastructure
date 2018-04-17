package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;

public class SimpleDiffAnalyzer extends DiffAnalyzer {

	private File file;

	public SimpleDiffAnalyzer(File file) {
		this.file = file;
	}

	@Override
	public Collection<FileEntry> parse() throws IOException {
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

}
