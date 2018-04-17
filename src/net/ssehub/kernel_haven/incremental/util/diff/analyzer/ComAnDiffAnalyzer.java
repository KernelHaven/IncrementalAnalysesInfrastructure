package net.ssehub.kernel_haven.incremental.util.diff.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import diff.BuildFileDiff;
import diff.FileDiff;
import diff.FileDiff.FileType;
import diff.ModelFileDiff;
import diff.OtherFileDiff;
import diff.SourceFileDiff;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;
import net.ssehub.kernel_haven.util.Logger;

/**
 * This class implements a general diff analyzer.<br>
 * <br>
 * 
 * This class is used to provide the numbers of changed lines in different file
 * types of a specific commit. In turn, this class uses the different file diff
 * classes depending on the type of file changed by the given commit.
 * 
 * 
 * @author Christian Kroeher
 *
 */
public class ComAnDiffAnalyzer extends DiffAnalyzer {

	private static Logger LOGGER = Logger.get();

	/**
	 * This array contains file extensions (without the ".") for identifying files
	 * that should not be analyzed.<br>
	 * <br>
	 * 
	 * Although regular expressions for identifying files for analysis exist, there
	 * are certain combinations that lead to wrong results, e.g. "Config.lb" (found
	 * in coreboot), where the name of the file seems to define a Kconfig-file, but
	 * the content is not.
	 */
	private static final String[] FILE_EXTENSION_BLACKLIST = { "lb" };

	/**
	 * String identifying the start of a new diff.<br>
	 * <br>
	 * 
	 * Each commit may include multiple diffs, each describing all changes to an
	 * individual file.<br>
	 * <br>
	 * 
	 * Value: {@value #DIFF_START_PATTERN};
	 */
	private static final String DIFF_START_PATTERN = "diff --git";

	/**
	 * String identifying the start of the first description of the actual changes
	 * of a diff.<br>
	 * <br>
	 * 
	 * Value: {@value #CHANGES_START_PATTERN};
	 */
	private static final String CHANGES_START_PATTERN = "@@";

	/**
	 * Regex identifying directories containing documentation.<br>
	 * <br>
	 * 
	 * Value: {@value #DOC_DIR_PATTERN};
	 */
	private static final String DOC_DIR_PATTERN = "[dD]ocumentation(s)?";

	/**
	 * Regex identifying directories containing scripts.<br>
	 * <br>
	 * 
	 * Value: {@value #SCRIPT_DIR_PATTERN};
	 */
	private static final String SCRIPT_DIR_PATTERN = "[sS]cript(s)?";

	/**
	 * Regex identifying files to be excluded from analysis, in particular
	 * documentation files or scripts.<br>
	 * <br>
	 * 
	 * Value: {@value #FILE_EXCLUDE_PATTERN};<br>
	 * 
	 * See {@link #DOC_DIR_PATTERN} and {@link #SCRIPT_DIR_PATTERN}
	 */
	private static final String FILE_EXCLUDE_PATTERN = "(.*/((" + DOC_DIR_PATTERN + ")|(" + SCRIPT_DIR_PATTERN
			+ "))/.*)|(.*\\.txt)";

	/**
	 * Regex identifying variability model files.<br>
	 * <br>
	 * 
	 * Value: {@value #MODEL_FILE_PATTERN};<br>
	 * <br>
	 * 
	 * Note: No support for busybox anymore due to constant changes in naming and
	 * using the variability model (files).
	 */
	private static final String MODEL_FILE_PATTERN = ".*/Kconfig((\\.|\\-|\\_|\\+|\\~).*)?";

	/**
	 * Regex identifying source code files.<br>
	 * <br>
	 * 
	 * Value: {@value #SOURCE_FILE_PATTERN};
	 */
	private static final String SOURCE_FILE_PATTERN = ".*/.*\\.[hcS]((\\.|\\-|\\_|\\+|\\~).*)?";

	/**
	 * Regex identifying build files.<br>
	 * <br>
	 * 
	 * Value: {@value #BUILD_FILE_PATTERN};
	 */
	private static final String BUILD_FILE_PATTERN = ".*/(Makefile|Kbuild)((\\.|\\-|\\_|\\+|\\~).*)?"; // |(.*/.*\\.(mak|make)))

	/**
	 * The {@link File} containing the diff information of a specific commit.<br>
	 * <br>
	 * Typically, the name of the file represents the commit SHA, e.g "0004e99.txt",
	 * where "0004e99" is the commit SHA.<br>
	 * <br>
	 * The expected content of the file is a list of diff information, e.g.:<br>
	 * <i>diff --git a/include/libbb.h b/include/libbb.h<br>
	 * index 6fb0438..4b69c85 100644<br>
	 * --- a/include/libbb.h<br>
	 * +++ b/include/libbb.h<br>
	 * 
	 * @@ -1575,0 +1576,10 @@ extern const char *applet_name;<br>
	 * [...]<br>
	 * diff --git [...]<br>
	 * [...]</i>
	 */
	private File commitFile = null;

	/**
	 * The date the commit was created.<br>
	 * <br>
	 * The content of each {@link #commitFile} starts with a line containing the
	 * date and time the specific commit was created, e.g. <i>2011-06-10 06:01:30
	 * +0200</i>. This property only contains the date in the format
	 * <i>dd/mm/yyyy</i>.
	 */
	private String commitDate = null;

	/**
	 * The commit SHA retrieved from {@link #commitFile} defining the commit to
	 * which the analyzed diffs belong to.
	 */
	private String commitFileName = null;

	/**
	 * Construct a new {@link ComAnDiffAnalyzer}.<br>
	 * <br>
	 * 
	 * <b>Important</b>: to work as expected the given file name must indicate a
	 * commit SHA and has to contain diff information only. Refer to
	 * {@link #commitFile} for further information.<br>
	 * 
	 * @param commitFile
	 *            the {@link File} containing diff information.
	 */
	public ComAnDiffAnalyzer(File commitFile) {
		this.commitFile = commitFile;

		commitFileName = commitFile.getName().substring(0, commitFile.getName().lastIndexOf("."));

	}

	/**
	 * Analyze the diff information of the given commit (file).
	 * 
	 * @return <code>true</code> if the analysis of the given commit was successful,
	 *         <code>false</code> otherwise, e.g. if given commit file does not
	 *         match expected name, extension, or does not include changes
	 */
	public Collection<FileEntry> parse() throws IOException {
		Collection<FileEntry> fileEntries = new ArrayList<FileEntry>();
		// Create list off diff entries where each entry in the list represents changes
		// for a single file
		List<String> diffList = createDiffList();
		FileDiff fileDiff = null;
		for (String diff : diffList) {
			FileEntry.Type type = null;
			FileEntry.VariabilityChange change = FileEntry.VariabilityChange.NOT_ANALYZED;
			String filePath = null;

			// Analyze for change Type
			List<String> lines = Arrays.asList(diff.split("\\r?\\n"));
			for (int i = 0; i + 1 < lines.size(); i++) {
				String currentLine = lines.get(i);
				if (currentLine.startsWith(DIFF_START_PATTERN)) {
					String nextLine = lines.get(i + 1);
					filePath = currentLine.substring(currentLine.indexOf("a/") + "a/".length(),
							currentLine.indexOf(" b/"));
					if (nextLine.startsWith("new file mode")) {
						type = FileEntry.Type.ADDITION;
						break;
					} else if (nextLine.startsWith("deleted file mode")) {
						type = FileEntry.Type.DELETION;
						break;
					} else {
						type = FileEntry.Type.MODIFICATION;
						break;
					}
				}
			}

			// Analyze for Variability_Change
			fileDiff = createFileDiff(diff);
			if (fileDiff != null) {
				if (!fileDiff.getFileType().equals(FileType.OTHER)) {
					// if the does contain conent representing the variability, build or code-model
					// check for changes in variability
					if (getChangedLines(fileDiff, true) > 0) {
						change = FileEntry.VariabilityChange.CHANGE;
					} else {
						change = FileEntry.VariabilityChange.NO_CHANGE;
					}
				} else {
					change = FileEntry.VariabilityChange.NOT_A_VARIABILITY_FILE;
				}
			}

			// Add new entry for the file currently considered
			fileEntries.add(new FileEntry(Paths.get(filePath), type, change));
		}

		return fileEntries;
	}

	/**
	 * Return the sum of changed lines of the given {@link FileDiff}; either all
	 * changed lines or only those lines that contain variability information (see
	 * <code>varLinesOnly</code>).
	 * 
	 * @param fileDiff
	 *            the {@link FileDiff} for which the sum of changed lines should be
	 *            calculated
	 * @param varLinesOnly
	 *            returns only those lines that contain variability information if
	 *            set to <code>true</code> or all changed lines if set to
	 *            <code>false</code>
	 * @return the sum of changed lines (all or variability only) of the given file
	 *         diff
	 */
	private int getChangedLines(FileDiff fileDiff, boolean varLinesOnly) {
		int changedLines = 0;
		if (varLinesOnly) {
			// Return sum of changed lines holding variability information only
			changedLines = fileDiff.getAddedVarLinesNum() + fileDiff.getDeletedVarLinesNum();
		} else {
			// Return sum of all changed lines
			changedLines = fileDiff.getAddedLinesNum() + fileDiff.getDeletedLinesNum();
		}
		return changedLines;
	}

	/**
	 * Create a new {@link FileDiff} based on the given <code>diff</code>
	 * information. The actual type of the returned <code>FileDiff</code> depends on
	 * the type of file under change as provided by the <code>diff</code>
	 * information:<br>
	 * <ul>
	 * <li>{@link SourceFileDiff}</li>
	 * <li>{@link BuidFileDiff}</li>
	 * <li>{@link ModelFileDiff}</li>
	 * </ul>
	 * <br>
	 * 
	 * @param diff
	 *            the diff text describing the changed of a specific file
	 * @return a {@link FileDiff} object holding detailed information about the
	 *         diff, e.g. number of changed lines
	 */
	private FileDiff createFileDiff(String diff) {
		FileDiff fileDiff = null;
		String diffLines[] = diff.split("\n");
		if (diffLines.length > 0) {
			/*
			 * First line contains the path to and the name of the changed file, e.g.:
			 * 
			 * diff --git a/<path>/<filename> b/<path>/<filename>
			 * 
			 * Thus, use this line to: a) check whether the path includes directories not of
			 * interest (documentation, scripts) b) identify the type of the file
			 * (variability model, source code, build)
			 */
			String changedFileDescriptionLine = diffLines[0];
			/*
			 * Each diff starts with some general information about the introduced changes,
			 * e.g.:
			 * 
			 * diff --git a/include/libbb.h b/include/libbb.h index 6fb0438..4b69c85 100644
			 * --- a/include/libbb.h +++ b/include/libbb.h
			 * 
			 * @@ -1575,0 +1576,10 @@ extern const char *applet_name; + +/* Some older
			 * linkers don't perform string merging, we used to have common strings ...
			 * 
			 * After identifying the name (and type) of the changed file, only the lines
			 * describing the actual changes to that file are of interest. Thus, skip the
			 * other lines containing general information. In the example, start with the
			 * line "@@ ..." for detailed analysis.
			 */
			int changesStartLine = getFirstChangeLine(diffLines);
			if (changesStartLine > -1 && changesStartLine < diffLines.length) {
				if (Pattern.matches(FILE_EXCLUDE_PATTERN, changedFileDescriptionLine)
						|| isBlacklisted(changedFileDescriptionLine)) {
					// Either excluded or blacklisted file changed, thus use OtherFileDiff
					fileDiff = new OtherFileDiff(diffLines, changesStartLine);
				} else if (Pattern.matches(SOURCE_FILE_PATTERN, changedFileDescriptionLine)) {
					fileDiff = new SourceFileDiff(diffLines, changesStartLine);
				} else if (Pattern.matches(BUILD_FILE_PATTERN, changedFileDescriptionLine)) {
					fileDiff = new BuildFileDiff(diffLines, changesStartLine);
				} else if (Pattern.matches(MODEL_FILE_PATTERN, changedFileDescriptionLine)) {
					fileDiff = new ModelFileDiff(diffLines, changesStartLine);
				} else {
					/*
					 * As this method should only return null if no changes start line can be
					 * identified, we need another way of excluding files not of interest. This is
					 * done by creating an OtherFileDiff-object, which is actually doing nothing and
					 * does not influence further analysis.
					 */
					fileDiff = new OtherFileDiff(diffLines, changesStartLine);
				}
			} else {
				LOGGER.logWarning("No changes found \n Commit \"" + commitFileName
						+ "\" includes diff without any line starting with \"@@\" indicating line changes");
			}
		}
		return fileDiff;
	}

	/**
	 * Check the name of the changed file defined in the given changed file
	 * description line against the blacklisted file extensions defined in
	 * {@link #FILE_EXTENSION_BLACKLIST}.
	 * 
	 * @param changedFileDescriptionLine
	 *            the first line of a diff containing the path and the name of the
	 *            changed file, e.g. "diff --git a/include/libbb.h
	 *            b/include/libbb.h"
	 * @return <code>true</code> if the extension of the file in the given changed
	 *         file description line matches on of the blacklisted file extensions,
	 *         <code>false</code> otherwise
	 */
	private boolean isBlacklisted(String changedFileDescriptionLine) {
		boolean isBlacklisted = false;
		int blacklistCounter = 0;
		while (blacklistCounter < FILE_EXTENSION_BLACKLIST.length && !isBlacklisted) {
			/*
			 * The given line always contains a string similar to
			 * "diff --git a/include/libbb.h b/include/libbb.h". Thus, remove leading and
			 * trailing whitespace and check if one of the blacklist entries prepended by a
			 * "." matched the end of the given line.
			 */
			String fileExtension = "." + FILE_EXTENSION_BLACKLIST[blacklistCounter];
			if (changedFileDescriptionLine.trim().endsWith(fileExtension)) {
				isBlacklisted = true;
			}
			blacklistCounter++;
		}
		return isBlacklisted;
	}

	/**
	 * Return the index of the line in the diff information part that marks the
	 * starting point of the change details in terms of added and removed lines.
	 * This line starts with {@value #CHANGES_START_PATTERN}.
	 * 
	 * @param diffLines
	 *            the diff information part in which the line marking the start of
	 *            change details should be found
	 * @return the index of the line in the diff information part marking the start
	 *         of change details or <code>-1</code> if this line could not be found.
	 */
	private int getFirstChangeLine(String[] diffLines) {
		int firstChangeLine = -1;
		int lineCounter = 0;
		while (firstChangeLine < 0 && lineCounter < diffLines.length) {
			if (diffLines[lineCounter].startsWith(CHANGES_START_PATTERN)) {
				firstChangeLine = lineCounter;
			}
			lineCounter++;
		}
		return firstChangeLine;
	}

	/**
	 * Create a list of diff information read from the defined commit file.
	 * 
	 * @return a {@link List} of strings, each containing a diff information (single
	 *         file diff)
	 * @throws IOException
	 * @see {@link #DiffAnalyzer(File)}
	 */
	private List<String> createDiffList() throws IOException {
		List<String> diffList = null;

		diffList = new ArrayList<String>();

		StringBuilder diffInfoBuilder = new StringBuilder();

		// We can not read lines (e.g. via Files.readAllLines(path)) to an array/list
		// and iterate over it as this fails
		// for huge input-files such as the initial commit for a bigger software-project
		try (BufferedReader br = new BufferedReader(new FileReader(commitFile))) {
			boolean firstLine = true;
			for (String fileLine; (fileLine = br.readLine()) != null;) {
				if (firstLine && !fileLine.startsWith(DIFF_START_PATTERN)) {
					parseCommitDate(fileLine);
					firstLine = false;
				} else if (fileLine.startsWith(DIFF_START_PATTERN)) {
					firstLine = false;
					if (diffInfoBuilder.length() > 0) {
						// Save current diff info to list
						diffList.add(diffInfoBuilder.toString());
					}
					diffInfoBuilder = new StringBuilder();
					diffInfoBuilder.append(fileLine);
				} else {
					diffInfoBuilder.append("\n" + fileLine);
				}
			}
		}

		// add diff-info for last entry in diff file
		diffList.add(diffInfoBuilder.toString());

		return diffList;
	}

	/**
	 * Parse the given content line to {@link #commitDate}.
	 * 
	 * @param commitFileFirstLine
	 *            the first line in the {@link #commitFile} containing the date and
	 *            time the commit was created, e.g. <i>2011-06-10 06:01:30 +0200</i>
	 */
	private void parseCommitDate(String commitFileFirstLine) {
		if (commitFileFirstLine != null && !commitFileFirstLine.isEmpty()) {
			// The line contains date and time like "2011-06-10 06:01:30 +0200"
			String[] dateAndTimeParts = commitFileFirstLine.split("\\s+");
			if (dateAndTimeParts.length > 0) {
				// Here, we only need the first part "2011-06-10" split into year, month, and
				// day
				String[] dateParts = dateAndTimeParts[0].split("-");
				if (dateParts.length == 3) {
					commitDate = dateParts[0] + "/" + dateParts[1] + "/" + dateParts[2];
				}
			}
		}
	}

	/**
	 * Return the commit SHA of the analyzed commit.
	 * 
	 * @return the commit SHA of the analyzed commit
	 */
	public String getCommitNumber() {
		return commitFileName;
	}

	/**
	 * Return the date the analyzed commit was created in the format
	 * <i>dd/mm/yyyy</i>.
	 * 
	 * @return the date the analyzed commit was created
	 */
	public String getCommitDate() {
		return commitDate;
	}

}
