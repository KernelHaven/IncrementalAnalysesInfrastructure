package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class IncrementalConfigurationGenerator {

	private static final String INCREMENTAL_TEMPLATE = "configuration-incremental.properties.template";
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		System.out.println("Enter KernelHaven base directory path (Absolute or relative to point of execution of "
				+ IncrementalConfigurationGenerator.class.getSimpleName() + ")");
		Path baseDir = Paths.get(scanner.nextLine());

		System.out.println("Enter folder path to diff-files folder (Absolute or relative to point of execution of "
				+ IncrementalConfigurationGenerator.class.getSimpleName() + ")");
		Path diffFilesDir = Paths.get(scanner.nextLine());

		System.out.println(
				"Enter path to folder where you want the generated configuration files to be stored (Absolute or relative to point of execution of "
						+ IncrementalConfigurationGenerator.class.getSimpleName() + ")");
		Path targetDir = Paths.get(scanner.nextLine());

		try {
			generateConfigurationFiles(baseDir, diffFilesDir, targetDir);
		} catch (FileNotFoundException e) {
			System.out.println("Could not generate configuration.");
			e.printStackTrace();
		}

	}

	private static void generateConfigurationFiles(Path baseDir, Path diffFilesDir, Path targetDir)
			throws FileNotFoundException {
		String[] diffFiles = diffFilesDir.toFile().list();

		Arrays.sort(diffFiles);
		String diffFileFolderString;

		if (isParent(baseDir, diffFilesDir)) {
			diffFileFolderString = diffFilesDir.relativize(diffFilesDir).toString();
		} else {
			diffFileFolderString = diffFilesDir.toAbsolutePath().toString();
		}

		for (String diffFile : diffFiles) {
			String diffFileString = diffFile;
			if (!diffFileFolderString.isEmpty()) {
				diffFileString += diffFileFolderString + "/";
			}
			if (!diffFile.endsWith(".parsed")) {
				try (PrintWriter out = new PrintWriter(targetDir.toAbsolutePath() + "/config-" + diffFile + ".properties")) {
					out.println(generateConfigForDiffFile(diffFileString, INCREMENTAL_TEMPLATE));
				}
			}

		}
	}

	private static String generateConfigForDiffFile(String diffFileString, String template) {
		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(IncrementalConfigurationGenerator.class.getResourceAsStream(template)))) {
			template = buffer.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return template.replace("${diff.file}", diffFileString);
	}

	private static boolean isParent(Path parent, Path child) {
		parent = parent.toAbsolutePath();
		child = child.toAbsolutePath();
		return child.startsWith(parent);
	}

}
