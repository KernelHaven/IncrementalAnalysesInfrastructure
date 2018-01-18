package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class FolderUtil {

	public static Collection<File> getNewOrChangedFiles(File referenceDirectory, File newDirectory) throws IOException {
		Path referenceDirectoryPath = referenceDirectory.getAbsoluteFile().toPath().normalize();
		Path newDirectorydPath = newDirectory.getAbsoluteFile().toPath().normalize();

		// Create a list of relative Paths to all files in newDirectory
		Collection<Path> pathsForFilesInNewDirectory = new ArrayList<Path>();
		FolderUtil.listFiles(newDirectorydPath.toFile(), true)
				.forEach(file -> pathsForFilesInNewDirectory.add(newDirectorydPath.relativize(file.toPath())));

		Collection<File> newOrChangedFiles = new ArrayList<File>();

		for (Path filePath : pathsForFilesInNewDirectory) {
			File fileInRefDir = referenceDirectoryPath.resolve(filePath).toFile();
			File fileInNewDir = newDirectorydPath.resolve(filePath).toFile();

			// if the file does not exist in the reference directory or the file content is
			// not equal add it to the list of changed files
			if (!fileInRefDir.exists() || !FileUtil.fileContentIsEqual(fileInRefDir, fileInNewDir)) {
				newOrChangedFiles.add(newDirectorydPath.relativize(fileInNewDir.toPath()).toFile());
			}
		}
		return newOrChangedFiles;
	}

	public static Collection<File> listFiles(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = new ArrayList<>();
		listFiles(directory, files, includeFilesInSubDirectories);
		return files;
	}

	public static Collection<File> listFilesRelativeToDirectory(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = listFiles(directory, includeFilesInSubDirectories);
		Path directoryPath = directory.toPath();
		for (File file : files) {
			file = file.toPath().relativize(directoryPath).toFile();
		}
		return files;
	}

	private static void listFiles(File directory, Collection<File> files, boolean recursive) {
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory() && recursive) {
				listFiles(file, files, recursive);
			}
		}
	}

}
