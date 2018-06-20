package net.ssehub.kernel_haven.incremental.diff;

import java.nio.file.Path;
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an entry for a file in the changeset. Used by {@link DiffFile} to
 * describe changes for a git-diff-file.
 * 
 * @author moritz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FileEntry {

	/**
	 * Type of change.
	 */
	public enum Type {

		/** Modification of a file. */
		MODIFICATION,

		/** Additition of a file. */
		ADDITION,

		/** Deletion of a file. */
		DELETION
	}

	public enum VariabilityChange {

		/** Indicates changed variability */
		CHANGE,

		/** Indicates no changed variability */
		NO_CHANGE,

		/**
		 * Indicates that the file was not considered to be a file carrying variability
		 * information
		 */
		NOT_A_VARIABILITY_FILE,

		/**
		 * Indicates that no analysis on variability information was performed on the
		 * file represented by this {@link FileEntry}
		 */
		NOT_ANALYZED
	}

	/** The file. */
	private Path file;

	/** The type. */
	private Type type;

	private VariabilityChange variabilityChange;

	/**
	 * Instantiates a new file entry.
	 *
	 * @param file
	 *            the file
	 * @param type
	 *            the type
	 */
	public FileEntry(Path file, Type type, VariabilityChange variabilityChange) {
		this.file = file;
		this.type = type;
		this.variabilityChange = variabilityChange;
	}


	/**
	 * Instantiates a new file entry.
	 *
	 * @param file
	 *            the file
	 * @param type
	 *            the type
	 */
	public FileEntry(Path file, Type type) {
		this.file = file;
		this.type = type;
		this.variabilityChange = VariabilityChange.NOT_ANALYZED;
	}

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

	public VariabilityChange getVariabilityChange() {
		return variabilityChange;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]").add("file = " + file)
				.add("type = " + type).add("variabilityChange = " + variabilityChange).toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variabilityChange == null) ? 0 : variabilityChange.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileEntry other = (FileEntry) obj;
		if (variabilityChange != other.variabilityChange)
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
