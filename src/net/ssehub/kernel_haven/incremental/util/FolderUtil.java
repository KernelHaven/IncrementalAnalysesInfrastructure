package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

public class FolderUtil {

	public static void copyFolderContent(File originalFolder, File targetFolder) throws IOException {
		Collection<File> files = FolderUtil.listFilesAndFolders(originalFolder, true);
		for (File file : files) {
			// create target path based on the relative path that the file has in the
			// originalFolder
			Path targetPath = targetFolder.toPath().resolve(originalFolder.toPath().relativize(file.toPath()));
			// create parent directories if they do not exist
			if (file.isDirectory()) {
				file.mkdir();
			} else {
				targetPath.toFile().getParentFile().mkdirs();
				Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	public static boolean folderContentEquals(File folderA, File folderB) throws IOException {
		boolean equals = true;
		Collection<File> filesFolderA = FolderUtil.listFilesAndFolders(folderA, true);
		Collection<File> filesFolderB = FolderUtil.listFilesAndFolders(folderB, true);
		equals = filesFolderA.size() == filesFolderB.size();
		if (equals) {
			for (File folderAFile : folderA.listFiles()) {
				File correspondingFileInB = folderB.toPath().resolve(folderA.toPath().relativize(folderAFile.toPath()))
						.toFile();
				equals = FileUtil.fileContentIsEqual(folderAFile, correspondingFileInB);
				if (!equals) {
					break;
				}
			}
		}
		return equals;
	}

	public static Collection<File> getNewOrChangedFiles(File referenceDirectory, File newDirectory) throws IOException {
		Path referenceDirectoryPath = referenceDirectory.getAbsoluteFile().toPath().normalize();
		Path newDirectorydPath = newDirectory.getAbsoluteFile().toPath().normalize();

		// Create a list of relative Paths to all files in newDirectory
		Collection<Path> pathsForFilesInNewDirectory = new ArrayList<Path>();
		for (File file : FolderUtil.listFilesAndFolders(newDirectorydPath.toFile(), true)) {
			if (!file.isDirectory()) {
				pathsForFilesInNewDirectory.add(newDirectorydPath.relativize(file.toPath()));
			}
		}

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

	public static Collection<File> listFilesAndFolders(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = new ArrayList<>();
		listFilesAndFolders(directory, files, includeFilesInSubDirectories);
		return files;
	}

	private static void listFilesAndFolders(File directory, Collection<File> files, boolean recursive) {
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory() && recursive) {
				files.add(file);
				listFilesAndFolders(file, files, recursive);
			}
		}
	}

}
