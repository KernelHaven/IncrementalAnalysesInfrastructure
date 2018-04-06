package net.ssehub.kernel_haven.incremental.util.diff;

import java.nio.file.Path;

public class FileEntry {

	
	public enum Type{
		MODIFICATION,
		ADDITION,
		DELETION
	}
	
	private Path file;
	private Type type;

	public Path getPath() {
		return file;
	}

	public Type getType() {
		return type;
	}

	public FileEntry(Path file, Type type) {
		super();
		this.file = file;
		this.type = type;
	}

	
	
}
