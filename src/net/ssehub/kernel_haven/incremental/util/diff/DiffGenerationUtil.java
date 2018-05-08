package net.ssehub.kernel_haven.incremental.util.diff;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

public class DiffGenerationUtil {

	public static final String EMPTY_REPOSITORY_HASH = "4b825dc642cb6eb9a060e54bf8d69288fbee4904";
	public static final String CURRENT_COMMIT_HASH = "HEAD";

	private static final Logger LOGGER = Logger.get();
	private File gitRepository;

	public DiffGenerationUtil(File gitRepository) {
		this.gitRepository = gitRepository;
	}

	public boolean generateDiff(String oldCommitHash, String newCommitHash, File resultFile) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "--no-renames", "--binary", oldCommitHash,
				newCommitHash);
		processBuilder.directory(gitRepository);

		FileOutputStream stdoutStream = new FileOutputStream(resultFile);
		ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

		boolean success = false;
		try {
			success = Util.executeProcess(processBuilder, "git diff", stdoutStream, stderrStream, 0);
		} catch (IOException e) {
			LOGGER.logException("Could not merge changes", e);
		}

		String stderr = stderrStream.toString();

		if (stderr != null && !stderr.equals("")) {
			if (!success) {
				LOGGER.logError(("git apply stderr:\n" + stderr).split("\n"));
			} else {
				LOGGER.logDebug(("git apply stderr:\n" + stderr).split("\n"));
			}
		}



		return success;
	}

	public boolean generateDiffs(List<String> commits, File outputDir) throws IOException {
		boolean success = true;

		String thisCommit = null;
		String nextCommit = null;

		outputDir.mkdirs();

		int counter = 1;
		for (String commit : commits) {
			thisCommit = nextCommit;
			nextCommit = commit;
			if (thisCommit != null) {
				String counterString = String.format("%05d", counter);
				File outputFile = outputDir.toPath().resolve(counterString + "-git.diff").toFile();
				generateDiff(thisCommit, nextCommit, outputFile);
				counter++;
			}
		}

		return success;
	}

	public List<String> listAllCommitsInRange(String startCommitHash, String endCommitHash) {
		ProcessBuilder processBuilder = new ProcessBuilder("git", "log", "--first-parent", "--pretty=oneline",
				startCommitHash + "^.." + endCommitHash);
		processBuilder.directory(gitRepository);

		ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
		ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

		boolean success = false;
		try {
			success = Util.executeProcess(processBuilder, "git diff", stdoutStream, stderrStream, 0);
		} catch (IOException e) {
			LOGGER.logException("Could not merge changes", e);
		}

		String stderr = stderrStream.toString();
		String stdout = stdoutStream.toString();

		if (stderr != null && !stderr.equals("")) {
			if (!success) {
				LOGGER.logError(("git apply stderr:\n" + stderr).split("\n"));
			} else {
				LOGGER.logDebug(("git apply stderr:\n" + stderr).split("\n"));
			}
		}

		if (success) {
			List<String> commits = Arrays.asList(stdout.split("\n"));
			Collections.reverse(commits);

			List<String> cleanedCommitLines = new ArrayList<String>();
			commits.forEach(commit -> cleanedCommitLines.add(commit.substring(0, EMPTY_REPOSITORY_HASH.length())));
			return cleanedCommitLines;
		} else {
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		File linuxRepo = new File("/home/moritz/Schreibtisch/linux-repo/linux");
		File outputDir = new File("/home/moritz/Schreibtisch/linux-repo/diffs");

		System.out.println("started");
		DiffGenerationUtil diffGen = new DiffGenerationUtil(linuxRepo);
		System.out.println("Creating list of commits in range ...");
		List<String> commits = diffGen.listAllCommitsInRange("4fbd8d194f06c8a3fd2af1ce560ddb31f7ec8323",
				"d8a5b80568a9cb66810e75b182018e9edb68e8ff");

		System.out.println("Creating list of commits with empty start ...");
		List<String> commitsWithEmptyStart = new ArrayList<String>();
		commitsWithEmptyStart.add(EMPTY_REPOSITORY_HASH);

		commitsWithEmptyStart.addAll(commits);

		System.out.println("Generating diffs for following commit-hashes:" +
				Arrays.toString(commitsWithEmptyStart.toArray()));

		diffGen.generateDiffs(commitsWithEmptyStart, outputDir);
	}

}
