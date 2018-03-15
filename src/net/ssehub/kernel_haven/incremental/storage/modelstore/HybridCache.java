package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.build_model.BuildModelCache;
import net.ssehub.kernel_haven.code_model.CodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityModelCache;

public class HybridCache {

	private static final Path CURRENT_CACHE_FOLDER = Paths.get("current/");
	private static final Path PREVIOUS_CACHE_FOLDER = Paths.get("previous/");;

	private static final Path[] BM_CACHE_FILES = { Paths.get("bmCache") };
	private static final Path[] VM_CACHE_FILES = { Paths.get("vmCache.variables"), Paths.get("vmCache.constraints") };
	private static final char CM_REPLACE_THIS = File.separatorChar;
	private static final char CM_REPLACE_WITH_THIS = '.';
	private static final String CM_CACHE_SUFFIX = ".cache";
	private static final Path ADDED_FOLDER = Paths.get("added-in-current/");

	private File currentFolder;
	private File previousFolder;
	private File addedFolder;

	private CodeModelCache currentCmCache;
	private VariabilityModelCache currentVmCache;
	private BuildModelCache currentBmCache;

	private CodeModelCache previousCmCache;
	private VariabilityModelCache previousVmCache;
	private BuildModelCache previousBmCache;

	public HybridCache(File cacheFolder) {
		this.currentFolder = cacheFolder.toPath().resolve(CURRENT_CACHE_FOLDER).toFile();
		this.previousFolder = cacheFolder.toPath().resolve(PREVIOUS_CACHE_FOLDER).toFile();
		this.addedFolder = previousFolder.toPath().resolve(ADDED_FOLDER).toFile();
		this.currentFolder.mkdirs();
		this.previousFolder.mkdirs();

		this.currentBmCache = new BuildModelCache(currentFolder);
		this.currentVmCache = new VariabilityModelCache(currentFolder);
		this.currentCmCache = new CodeModelCache(currentFolder);
		this.previousCmCache = new CodeModelCache(previousFolder);
		this.previousVmCache = new VariabilityModelCache(previousFolder);
		this.previousBmCache = new BuildModelCache(previousFolder);

	}

	public void clearPrevious() {
		for (File file : previousFolder.listFiles()) {
			file.delete();
		}
	}

	public void write(SourceFile file) throws IOException {
		String fileNameInCache = file.getPath().toString().replace(CM_REPLACE_THIS, CM_REPLACE_WITH_THIS)
				+ CM_CACHE_SUFFIX;
		File fileToAdd = currentFolder.toPath().resolve(fileNameInCache).toFile();
		if (fileToAdd.exists()) {
			hybridDelete(fileToAdd);
		} else {
			hybridAdd(fileToAdd);
		}
		currentCmCache.write(file);
	}

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

	public SourceFile readCm(File target) throws IOException, FormatException {
		return currentCmCache.read(target);

	}

	public BuildModel readBm() throws FormatException, IOException {
		return currentBmCache.read(new File("not-used"));

	}

	public VariabilityModel readVm() throws FormatException, IOException {
		return currentVmCache.read(new File("not-used"));

	}

	public SourceFile readPreviousCm(File target) throws IOException, FormatException {
		SourceFile result = null;
		if (previousFolder.toPath().resolve(target.toPath()).toFile().exists()) {
			result = previousCmCache.read(target);
		} else if (previousFolder.toPath().resolve(target.toPath()).toFile().exists()
				&& !(addedFolder.toPath().resolve(target.toPath()).toFile().exists())){
			result = currentCmCache.read(target);
		}
		return result;

	}

	public BuildModel readPreviousBm() {
		BuildModel result = null;

		
		
		return result;

	}

	public VariabilityModel readPreviousVm() {
		VariabilityModel result = null;
		
		
		return result;
	}

	private void hybridAdd(File target) throws IOException {
		File deleteOnRollback = addedFolder.toPath().resolve(target.toPath()).toFile();
		deleteOnRollback.createNewFile();
	}

	public void hybridDelete(File target) throws IOException {
		File fileToDelete = currentFolder.toPath().resolve(target.toPath()).toFile();
		boolean doDelete = fileToDelete.exists();
		if (doDelete) {
			Files.move(fileToDelete.toPath(), previousFolder.toPath().resolve(target.toPath()));
		}
	}

	public void rollback() {

	}

}
