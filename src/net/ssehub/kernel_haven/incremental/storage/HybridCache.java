package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.build_model.BuildModelCache;
import net.ssehub.kernel_haven.code_model.CodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityModelCache;

/**
 * HybridCache serves the purpose of storing two different versions of the
 * models. Starting off with the previously present model (an empty model is
 * also possible), one can add or delete elements within that model. When a
 * modification takes place, the changes are persisted allowing for continuous
 * access to the previous model. As a HybridCache only stores two models
 * clearing information for the previous model via
 * {@link HybridCache#clearChangeHistory()} offers you the option to clear the
 * change-history thereby resulting in the previous model being equivalent to
 * the current model at the time of the method-call. Any subsequent
 * modifications will then again be stored for the previous model.
 * 
 */
public class HybridCache {

	/**
	 * Stores cache-files for the current models.
	 */
	private static final Path CURRENT_CACHE_FOLDER = Paths.get("current/");

	/**
	 * The folder represented by this path stores cache-files that replaced files in
	 * the current model. Those files can be used to access the previous model.
	 */
	private static final Path REPLACED_CACHE_FOLDER = Paths.get("history/replaced/");

	/**
	 * The folder represented by this path stores cache-files that were added in the
	 * current model and did not replace old files.
	 */
	private static final Path ADDED_FOLDER = Paths.get("history/added/");

	/** Subpaths for all files representing the build-model cache. */
	private static final Path[] BM_CACHE_FILES = { Paths.get("bmCache") };

	/** Subpaths for all files representing the variability-model cache. */
	private static final Path[] VM_CACHE_FILES = { Paths.get("vmCache.variables"), Paths.get("vmCache.constraints") };

	/**
	 * As filenames for the CodeModel-cache are generated out of the paths of
	 * source-files, this constant defines the chars to replace in the
	 * source-file-path.
	 */
	private static final char CM_REPLACE_THIS = File.separatorChar;

	/**
	 * As filenames for the CodeModel-cache are generated out of the paths of
	 * source-files, this constant defines the replacement for chars defined in
	 * {@link HybridCache#CM_REPLACE_THIS for the source-file-path.
	 */
	private static final char CM_REPLACEMENT = '.';

	/**
	 * Suffix for cache-files of the cm-cache.
	 */
	private static final String CM_CACHE_SUFFIX = ".cache";

	/** The current folder. */
	private File currentFolder;

	/** The replaced folder. */
	private File replacedFolder;

	/** The added folder. */
	private File addedFolder;

	/**
	 * Cache-Object for accessing cm-cache elements in
	 * {@link HybridCache#currentFolder}.
	 */
	private CodeModelCache currentCmCache;

	/**
	 * Cache-Object for accessing vm-cache elements in
	 * {@link HybridCache#currentFolder}.
	 */
	private VariabilityModelCache currentVmCache;

	/**
	 * Cache-Object for accessing bm-cache elements in
	 * {@link HybridCache#currentFolder}.
	 */
	private BuildModelCache currentBmCache;

	/**
	 * Cache-Object for accessing cm-cache elements in
	 * {@link HybridCache#replacedFolder}.
	 */
	private CodeModelCache replacedCmCache;

	/**
	 * Cache-Object for accessing vm-cache elements in
	 * {@link HybridCache#replacedFolder}.
	 */
	private VariabilityModelCache previousVmCache;

	/**
	 * Cache-Object for accessing bm-cache elements in
	 * {@link HybridCache#replacedFolder}.
	 */
	private BuildModelCache replacedBmCache;
	

	/**
	 * Instantiates a new hybrid cache.
	 *
	 * @param cacheFolder
	 *            the cache folder
	 */
	public HybridCache(File cacheFolder) {
		this.currentFolder = cacheFolder.toPath().resolve(CURRENT_CACHE_FOLDER).toFile();
		this.replacedFolder = cacheFolder.toPath().resolve(REPLACED_CACHE_FOLDER).toFile();
		this.addedFolder = cacheFolder.toPath().resolve(ADDED_FOLDER).toFile();
		this.addedFolder.mkdirs();
		this.currentFolder.mkdirs();
		this.replacedFolder.mkdirs();

		this.currentBmCache = new BuildModelCache(currentFolder);
		this.currentVmCache = new VariabilityModelCache(currentFolder);
		this.currentCmCache = new CodeModelCache(currentFolder);
		this.replacedCmCache = new CodeModelCache(replacedFolder);
		this.previousVmCache = new VariabilityModelCache(replacedFolder);
		this.replacedBmCache = new BuildModelCache(replacedFolder);
	}

	/**
	 * Removes all files representing the previous model.
	 */
	public void clearChangeHistory() {
		FolderUtil.deleteFolderContents(replacedFolder);
		FolderUtil.deleteFolderContents(addedFolder);
	}

	/**
	 * Write a {@link SourceFile} to the cache replacing any existing model
	 * accessible via {@link HybridCache#readCm()} with the same
	 * {@link SourceFile#getPath()}. The previous model can thereafter be accessed
	 * through {@link HybridCache#readPreviousCm(File)}
	 * 
	 *
	 * @param file
	 *            the source file to write to the cache.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(SourceFile file) throws IOException {
		String fileNameInCache = file.getPath().toString().replace(CM_REPLACE_THIS, CM_REPLACEMENT) + CM_CACHE_SUFFIX;
		File fileToAdd = currentFolder.toPath().resolve(fileNameInCache).toFile();
		if (fileToAdd.exists()) {
			hybridDelete(fileToAdd);
		} else {
			hybridAdd(fileToAdd);
		}
		currentCmCache.write(file);
	}

	/**
	 * Write the {@link VariabilityModel} to the cache replacing the model
	 * accessible via {@link HybridCache#readVm()}. The previous model can
	 * thereafter be accessed through {@link HybridCache#readPreviousVm()}
	 *
	 * @param vmModel
	 *            the vm model to write to the cache.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(VariabilityModel vmModel) throws IOException {
		for (Path add : VM_CACHE_FILES) {
			File fileToAdd = currentFolder.toPath().resolve(add).toFile();
			if (fileToAdd.exists()) {
				hybridDelete(fileToAdd);
			} else {
				hybridAdd(fileToAdd);
			}
		}
		currentVmCache.write(vmModel);
	}

	/**
	 * Write the {@link BuildModel} to the cache replacing the model accessible via
	 * {@link HybridCache#readBm()}. The previous model can thereafter be accessed
	 * through {@link HybridCache#readPreviousBm()}
	 *
	 * @param buildModel
	 *            the build model
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(BuildModel buildModel) throws IOException {
		for (Path add : BM_CACHE_FILES) {
			File fileToAdd = currentFolder.toPath().resolve(add).toFile();
			if (fileToAdd.exists()) {
				hybridDelete(fileToAdd);
			} else {
				hybridAdd(fileToAdd);
			}
		}
		currentBmCache.write(buildModel);
	}

	/**
	 * Read cm in current version.
	 *
	 * @param target
	 *            the target
	 * @return the source file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FormatException
	 *             the format exception
	 */
	public SourceFile readCm(File target) throws IOException, FormatException {
		return currentCmCache.read(target);

	}
	

	
	/**
	 * Read bm in current version.
	 *
	 * @return the builds the model
	 * @throws FormatException
	 *             the format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public BuildModel readBm() throws FormatException, IOException {
		return currentBmCache.read(new File("not-used"));

	}

	/**
	 * Read vm in current version.
	 *
	 * @return the variability model
	 * @throws FormatException
	 *             the format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public VariabilityModel readVm() throws FormatException, IOException {
		return currentVmCache.read(new File("not-used"));

	}

	/**
	 * Read cm in previous version.
	 *
	 * @param target
	 *            the target
	 * @return the source file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FormatException
	 *             the format exception
	 */
	public SourceFile readPreviousCm(File target) throws IOException, FormatException {
		SourceFile result = null;
		if (replacedFolder.toPath().resolve(target.toPath()).toFile().exists()) {
			result = replacedCmCache.read(target);
		} else if (replacedFolder.toPath().resolve(target.toPath()).toFile().exists()
				&& !(addedFolder.toPath().resolve(target.toPath()).toFile().exists())) {
			result = currentCmCache.read(target);
		}
		return result;

	}

	/**
	 * Read bm in previous version.
	 *
	 * @return the builds the model
	 * @throws FormatException
	 *             the format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public BuildModel readPreviousBm() throws FormatException, IOException {
		boolean existsInPrevious = existsInReplaced(BM_CACHE_FILES);

		BuildModel result = null;
		if (existsInPrevious) {
			result = replacedBmCache.read(new File("not-used"));
		} else {
			result = currentBmCache.read(new File("not-used"));
		}
		return result;

	}

	/**
	 * Read vm in previous version.
	 *
	 * @return the variability model
	 * @throws FormatException
	 *             the format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public VariabilityModel readPreviousVm() throws FormatException, IOException {
		boolean existsInPrevious = existsInReplaced(VM_CACHE_FILES);

		VariabilityModel result = null;
		if (existsInPrevious) {
			result = previousVmCache.read(new File("not-used"));
		} else {
			result = currentVmCache.read(new File("not-used"));
		}

		return result;
	}

	/**
	 * Checks if the paths exist as subpaths within the replaced-folder.
	 *
	 * @param paths
	 *            the paths
	 * @return true, if successful
	 */
	private boolean existsInReplaced(Path[] paths) {
		boolean existsInPrevious = true;
		for (Path bmFile : paths) {
			if (existsInPrevious) {
				existsInPrevious = REPLACED_CACHE_FOLDER.resolve(bmFile).toFile().exists();
				if (!existsInPrevious) {
					break;
				}
			}
		}
		return existsInPrevious;
	}

	/**
	 * Marks a file as added representing the changes between current and previous
	 * version.
	 *
	 * @param target
	 *            the target
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void hybridAdd(File target) throws IOException {
		File deleteOnRollback = addedFolder.toPath().resolve(target.toPath()).toFile();
		deleteOnRollback.createNewFile();
	}

	/**
	 * Moves a file that is deleted from the current model so that it can be
	 * accessed via functions like {@link HybridCache#readPreviousCm(File)},
	 * {@link HybridCache#readPreviousVm()} or {@link HybridCache#readPreviousBm()}.
	 *
	 * @param target
	 *            the target
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void hybridDelete(File target) throws IOException {
		File fileToDelete = currentFolder.toPath().resolve(target.toPath()).toFile();
		boolean doDelete = fileToDelete.exists();
		if (doDelete) {
			Files.move(fileToDelete.toPath(), replacedFolder.toPath().resolve(target.toPath()));
		}
	}

	/**
	 * Delete code model for a code-file within the source-tree.
	 *
	 * @param codeFileWithinSourceTree
	 *            the path
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void deleteCodeModel(File codeFileWithinSourceTree) throws IOException {
		hybridDelete(new File(
				codeFileWithinSourceTree.getPath().replace(CM_REPLACE_THIS, CM_REPLACEMENT) + CM_CACHE_SUFFIX));
	}

	/**
	 * Read all previous cm.
	 *
	 * @return the collection
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FormatException the format exception
	 */
	public Collection<SourceFile> readAllPreviousCm() throws IOException, FormatException {
		Collection<File> files = FolderUtil.listRelativeFiles(currentFolder, false);
		files.addAll(FolderUtil.listRelativeFiles(replacedFolder, false));
		files.removeAll(FolderUtil.listRelativeFiles(addedFolder, false));
		Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
		for (File file : files) {
			if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
				sourceFiles.add(readPreviousCm(file));
			}
		}
		return sourceFiles;
	}

	/**
	 * Read all cm.
	 *
	 * @return the collection
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FormatException the format exception
	 */
	public Collection<SourceFile> readAllCm() throws IOException, FormatException {
		Collection<File> files = FolderUtil.listRelativeFiles(currentFolder, false);
		Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
		for (File file : files) {
			if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
				sourceFiles.add(readCm(file));
			}
		}
		return sourceFiles;
	}
	
	/**
	 * Read changed cm.
	 *
	 * @return the collection
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws FormatException the format exception
	 */
	public Collection<SourceFile> readChangedCm() throws IOException, FormatException {
		Collection<File> files = FolderUtil.listRelativeFiles(replacedFolder, false);
		files.addAll(FolderUtil.listRelativeFiles(addedFolder, false));
		Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
		for (File file : files) {
			if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
				sourceFiles.add(readCm(file));
			}
		}
		return sourceFiles;
	}

	/**
	 * Delete build model.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void deleteBuildModel() throws IOException {
		for (Path path : BM_CACHE_FILES) {
			hybridDelete(path.toFile());
		}

	}

	/**
	 * Delete variability model.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void deleteVariabilityModel() throws IOException {
		for (Path path : VM_CACHE_FILES) {
			hybridDelete(path.toFile());
		}
	}

	/**
	 * Rollback to previous version. This reverts all changes made for the current
	 * version. Every modification is undone and the previous model will get reset
	 * after it entirely replaced the current model.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void rollback() throws IOException {

		// Delete newly added files
		for (File file : FolderUtil.listRelativeFiles(addedFolder, true)) {
			currentFolder.toPath().resolve(file.toPath()).toFile().delete();
		}

		// Move files that got replaced in current
		for (File file : FolderUtil.listRelativeFiles(replacedFolder, true)) {
			Files.move(replacedFolder.toPath().resolve(file.toPath()), currentFolder.toPath().resolve(file.toPath()),
					StandardCopyOption.REPLACE_EXISTING);
		}

		this.clearChangeHistory();

	}

}
