package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class FolderUtil.
 * 
 * @author Moritz
 */
public class FolderUtil {

	/**
	 * Copy folder content.
	 *
	 * @param sourceFolder
	 *            the source folder
	 * @param targetFolder
	 *            the target folder
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void copyFolderContent(File sourceFolder, File targetFolder) throws IOException {
		sourceFolder = sourceFolder.getAbsoluteFile();
		targetFolder = targetFolder.getAbsoluteFile();
		Collection<File> files = FolderUtil.listAbsoluteFilesAndFolders(sourceFolder, true);
		for (File file : files) {
			// create target path based on the relative path that the file has in the source
			// folder
			Path targetPath = targetFolder.toPath().resolve(sourceFolder.toPath().relativize(file.toPath()));
			// create parent directories if they do not exist
			if (file.isDirectory()) {
				file.mkdir();
			} else {
				targetPath.toFile().getParentFile().mkdirs();
				Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	/**
	 * Checks if folder content is equal.
	 *
	 * @param folderA
	 *            the folder A
	 * @param folderB
	 *            the folder B
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean folderContentEquals(File folderA, File folderB) throws IOException {
		boolean equals = true;
		Collection<File> filesFolderA = FolderUtil.listAbsoluteFilesAndFolders(folderA, true);
		Collection<File> filesFolderB = FolderUtil.listAbsoluteFilesAndFolders(folderB, true);
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

	public static void deleteFolderContents(File folder) {
		deleteFolder(folder, false);
	}

	public static void deleteFolder(File folder) {
		deleteFolder(folder, true);
	}

	private static void deleteFolder(File folder, boolean deleteRootFolder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFolder(file, true);
				} else {
					file.delete();
				}
			}
		}

		if (deleteRootFolder && folder.exists()) {
			folder.delete();
		}
	}

	/**
	 * Gets the new or changed files from newDirectory compared to
	 * referenceDirectory. This does not list deleted files.
	 *
	 * @param referenceDirectory
	 *            the reference directory
	 * @param newDirectory
	 *            the new directory
	 * @return the new or changed files
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Collection<File> getNewOrChangedFiles(File referenceDirectory, File newDirectory,
			boolean absolutePaths) throws IOException {
		Path referenceDirectoryPath = referenceDirectory.getAbsoluteFile().toPath();
		Path newDirectorydPath = newDirectory.getAbsoluteFile().toPath();

		// Create a list of relative Paths to all files in newDirectory
		Collection<Path> pathsForFilesInNewDirectory = new ArrayList<Path>();
		for (File file : FolderUtil.listAbsoluteFilesAndFolders(newDirectorydPath.toFile(), true)) {
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
				if (absolutePaths) {
					newOrChangedFiles.add(fileInNewDir);
				} else {
					newOrChangedFiles.add(filePath.toFile());
				}
			}
		}
		return newOrChangedFiles;
	}

	/**
	 * List files and folders within a directory.
	 *
	 * @param directory
	 *            the directory
	 * @param includeFilesInSubDirectories
	 *            the include files in sub directories
	 * @return the collection
	 */
	public static Collection<File> listAbsoluteFilesAndFolders(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = new ArrayList<>();
		listFilesAndFolders(directory, files, includeFilesInSubDirectories, true, true, directory);
		return files;
	}

	public static Collection<File> listRelativeFilesAndFolders(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = new ArrayList<>();
		listFilesAndFolders(directory, files, includeFilesInSubDirectories, true, false, directory);
		return files;
	}

	/**
	 * List files and folders within a directory.
	 *
	 * @param directory
	 *            the directory
	 * @param includeFilesInSubDirectories
	 *            the include files in sub directories
	 * @return the collection
	 */
	public static Collection<File> listAbsoluteFiles(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = new ArrayList<>();
		listFilesAndFolders(directory, files, includeFilesInSubDirectories, false, true, directory);
		return files;
	}

	public static Collection<File> listRelativeFiles(File directory, boolean includeFilesInSubDirectories) {
		Collection<File> files = new ArrayList<>();
		listFilesAndFolders(directory, files, includeFilesInSubDirectories, false, false, directory);
		return files;
	}

	/**
	 * This method is used by listFilesAndFolders to recursively determine the
	 * folder content.
	 *
	 * @param directory
	 *            the directory
	 * @param files
	 *            the files
	 * @param recursive
	 *            the recursive
	 */
	private static void listFilesAndFolders(File directory, Collection<File> files, boolean recursive,
			boolean listFolders, boolean absoluteFiles, File rootDirectory) {
		File[] fList = directory.listFiles();
		for (File file : fList) {
			File fileToAdd = file.getAbsoluteFile();
			if (!absoluteFiles) {
				fileToAdd = rootDirectory.toPath().relativize(file.toPath()).toFile();
			}
			if (file.isFile()) {
				files.add(fileToAdd);
			} else if (file.isDirectory()) {
				if (listFolders) {
					files.add(fileToAdd);
				}
				if (recursive) {
					listFilesAndFolders(file, files, recursive, listFolders, absoluteFiles, rootDirectory);
				}
			}
		}
	}

}
