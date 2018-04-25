package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.util.Logger;

public class ParsedDiffAnalyzer implements DiffAnalyzer {

	public static DiffFile generateDiffFile(File file) throws IOException {
		Logger.get().logWarning("You are using the " + ParsedDiffAnalyzer.class.getName()
				+ " which assumes that the diff-file already got parsed and is stored in a file named \"[incremental.input.source_tree_diff].parsed\". "
				+ "This " + DiffAnalyzer.class.getSimpleName()
				+ " is mainly there to support debugging and should not be used in productive Scenarios");
		File parsedDiffFile = new File(file.getAbsolutePath() + ".parsed");
		try {
			return DiffFile.load(parsedDiffFile);
		} catch (JAXBException e) {
			Logger.get().logException("Could not load diff-file from " + parsedDiffFile.getPath() + " with "
					+ ParsedDiffAnalyzer.class.getSimpleName(), e);
			return null;
		}
	}
}
