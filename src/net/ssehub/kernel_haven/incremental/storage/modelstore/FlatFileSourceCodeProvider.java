package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.File;

public class FlatFileSourceCodeProvider {
	
	private File rootDirectory;
	private String revision;
	
	
	
	public FlatFileSourceCodeProvider(File rootDirectory, String revision) {
		super();
		this.rootDirectory = rootDirectory;
		this.revision = revision;
	}
	
	public boolean hasFile(File file) {
		File absoluteFile = new File(
				rootDirectory.getAbsolutePath() + "/" + revision + "/" + file.getPath());
		return file.exists() || absoluteFile.exists();
	}
	
	

}
