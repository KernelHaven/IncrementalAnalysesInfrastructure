package net.ssehub.kernel_haven.incremental.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import net.ssehub.kernel_haven.incremental.diff.FileEntry.Type;
import net.ssehub.kernel_haven.incremental.diff.FileEntry.VariabilityChange;
import net.ssehub.kernel_haven.incremental.util.FileUtil;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * This class represents changes extracted from a diff-file.
 * 
 * @author Moritz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DiffFile {

	/** The diff. */
	Collection<FileEntry> changeSet;

	/**
	 * Instantiates a new diff file reader. The file passed to this constructor must
	 * be a git diff file.
	 *
	 * @param changeSet
	 *            the change set
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public DiffFile(@NonNull Collection<FileEntry> changeSet) throws IOException {
		this.changeSet = changeSet;
	}

	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	public Collection<FileEntry> getEntries() {
		return changeSet;
	}

	/**
	 * Saves the {@link DiffFile} object to filesystem.
	 *
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void save(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}

		StringBuilder builder = new StringBuilder();
		for (FileEntry entry : changeSet) {
			String type = null;
			switch (entry.getType()) {
			case ADDITION:
				type = "ADDITION";
				break;
			case DELETION:
				type = "DELETION";
				break;
			case MODIFICATION:
				type = "MODIFICATION";
				break;
			default:
				type = "UNDEFINED";
			}

			String variabilityChange = null;
			switch (entry.getVariabilityChange()) {
			case CHANGE:
				variabilityChange = "CHANGE";
				break;
			case NO_CHANGE:
				variabilityChange = "NO_CHANGE";
				break;
			case NOT_A_VARIABILITY_FILE:
				variabilityChange = "NOT_A_VARIABILITY_FILE";
				break;
			default:
				variabilityChange = "NOT_ANALYZED";
			}

			builder.append(new StringJoiner(",").add(entry.getPath().toString()).add(type).add(variabilityChange));
			builder.append("\n");
		}
		FileUtil.writeFile(file, builder.toString());
	}

	/**
	 * Loads the {@link DiffFile} object from filesystem.
	 *
	 * @param file
	 *            the file
	 * @return the diff file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParseException
	 *             the parse exception
	 */
	public static DiffFile load(File file) throws IOException, ParseException {
		DiffFile returnedDiff = null;
		Collection<FileEntry> entries = null;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			entries = new ArrayList<FileEntry>();
			for (String line; (line = br.readLine()) != null;) {
				String[] lineElements = line.split(",");
				if (lineElements.length == 3) {
					Path path = Paths.get(lineElements[0]);
					Type type;
					switch (lineElements[1]) {
					case "ADDITION":
						type = Type.ADDITION;
						break;
					case "DELETION":
						type = Type.DELETION;
						break;
					case "MODIFICATION":
						type = Type.MODIFICATION;
						break;
					default:
						throw new ParseException(
								"Undefined change-type in file: " + file.getAbsolutePath() + "\n" + line, 0);
					}

					VariabilityChange change;
					switch (lineElements[1]) {
					case "CHANGE":
						change = VariabilityChange.CHANGE;
						break;
					case "NO_CHANGE":
						change = VariabilityChange.NO_CHANGE;
						break;
					case "NOT_A_VARIABILITY_FILE":
						change = VariabilityChange.NOT_A_VARIABILITY_FILE;
						break;
					default:
						change = VariabilityChange.NOT_ANALYZED;
					}
					entries.add(new FileEntry(path, type, change));
				}

			}
			returnedDiff = new DiffFile(entries);
		}

		return returnedDiff;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changeSet == null) ? 0 : changeSet.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffFile other = (DiffFile) obj;
		if (changeSet == null) {
			if (other.changeSet != null)
				return false;
		} else if (!changeSet.containsAll(other.changeSet) && changeSet.size() != other.changeSet.size())
			return false;
		return true;
	}

}
