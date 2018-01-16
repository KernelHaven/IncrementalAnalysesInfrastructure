package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.ssehub.kernel_haven.code_model.CodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;

public class FlatFileCodeModelStorageProvider extends AbstractModelStorage<SourceFile> {

	private File modelStorageDir;

	private static final Logger LOGGER = Logger.get();

	public FlatFileCodeModelStorageProvider(File repoPath) {
		this.modelStorageDir = repoPath;
	}

	@Override
	public Collection<SourceFile> getModel(String tag) throws IOException {

		File dirForTag = new File(modelStorageDir.getAbsolutePath() + "/" + tag);
		Collection<SourceFile> codeModel = null;

		// Check if the requested tag is present in the file hierarchy
		if (dirForTag.exists() && dirForTag.isDirectory()) {
			codeModel = new ArrayList<SourceFile>();
			CodeModelCache cache = new CodeModelCache(dirForTag);

			// Create a list of all files in the directory and its subdirectories
			Collection<File> fileList = new ArrayList<File>();
			Files.newDirectoryStream(dirForTag.toPath(), path -> path.toFile().isFile())
					.forEach(path -> fileList.add(path.toFile()));

			try {
				// Read the model from each file
				for (File file : fileList) {
					codeModel.add(cache.read(file));
				}
			} catch (IOException | FormatException e) {
				LOGGER.logError("Could not read model from cached file " + cache);
			}

		}
		return codeModel;
	}

	public Map<File, SourceFile> getModelAsMap(String tag) throws IOException {

		File dirForTag = new File(modelStorageDir.getAbsolutePath() + "/" + tag);
		HashMap<File, SourceFile> codeModel = new HashMap<>();

		// Check if the requested tag is present in the file hierarchy
		if (dirForTag.exists() && dirForTag.isDirectory()) {
			CodeModelCache cache = new CodeModelCache(dirForTag);

			// Create a list of all files in the directory and its subdirectories
			Collection<File> fileList = new ArrayList<File>();
			Files.newDirectoryStream(dirForTag.toPath(), path -> path.toFile().isFile())
					.forEach(path -> fileList.add(path.toFile()));

			// Read the model from each file
			for (File file : fileList) {
				SourceFile srcFile;
				try {
					srcFile = cache.read(file);
					codeModel.put(srcFile.getPath(), srcFile);
				} catch (FormatException e) {
					LOGGER.logException("Could not read file " + file.getName(), e);
				}
			}

		}
		return codeModel;
	}

	@Override
	public void storeModelForTag(Collection<SourceFile> files, String tag) throws IOException {
		File storageDir = new File(this.modelStorageDir.getAbsolutePath() + "/" + tag + "/");
		CodeModelCache cache = new CodeModelCache(storageDir);
		for (SourceFile file : files) {
			cache.write(file);
		}
	}

}
