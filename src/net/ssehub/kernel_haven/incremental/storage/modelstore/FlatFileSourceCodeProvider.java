package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;

import net.ssehub.kernel_haven.incremental.util.FolderUtil;

public class FlatFileSourceCodeProvider {

	private File rootDirectory;
	private String revision;

	public FlatFileSourceCodeProvider(File rootDirectory, String revision) {
		super();
		this.rootDirectory = rootDirectory;
		this.revision = revision;
	}

	public boolean hasFile(File file) {
		File absoluteFile = new File(rootDirectory.getAbsolutePath() + "/" + revision + "/" + file.getPath());
		return file.exists() || absoluteFile.exists();
	}

	public Collection<File> getChangedOrAddedFiles(String referenceRevision) throws IOException {
		File referenceRevisionFolder = new File(rootDirectory.getAbsolutePath() + "/" + referenceRevision + "/");
		return FolderUtil.getNewOrChangedFiles(referenceRevisionFolder, this.rootDirectory);
	}

	public Collection<File> getAllFiles() {
		return FolderUtil.listFiles(rootDirectory, true);
	}

	public void writeFileToFilesystem(File sourceCodeProviderFile, File targetDirectory) throws IOException {
		File sourceFile = rootDirectory.toPath().resolve(sourceCodeProviderFile.toPath()).toFile();
		File targetFile = targetDirectory.toPath().resolve(sourceCodeProviderFile.toPath()).toFile();
		targetFile.mkdirs();
		targetFile.createNewFile();
		FileInputStream fileInput = new FileInputStream(sourceFile);
		FileOutputStream fileOutput = new FileOutputStream(targetFile);
		FileChannel src = fileInput.getChannel();
		FileChannel dest = fileOutput.getChannel();
		dest.transferFrom(src, 0, src.size());
		fileInput.close();
		fileOutput.close();
	}

}
