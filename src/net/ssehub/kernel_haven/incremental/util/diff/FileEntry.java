package net.ssehub.kernel_haven.incremental.util.diff;

import java.nio.file.Path;

/**
 * Represents an entry for a file in the changeset. Used by {@link DiffFile} to 
 * describe changes for a git-diff-file.
 * */
public class FileEntry {

	
	/**
	 * Type of change.
	 */
	public enum Type{
		
		/** Modification of a file. */
		MODIFICATION,
		
		/** Additition of a file. */
		ADDITION,
		
		/** Deletion of a file. */
		DELETION
	}
	
	/** The file. */
	private Path file;
	
	/** The type. */
	private Type type;

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public Path getPath() {
		return file;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Instantiates a new file entry.
	 *
	 * @param file the file
	 * @param type the type
	 */
	public FileEntry(Path file, Type type) {
		super();
		this.file = file;
		this.type = type;
	}

	
	
}
