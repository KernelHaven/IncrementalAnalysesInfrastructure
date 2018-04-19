package net.ssehub.kernel_haven.incremental.util.diff;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import net.ssehub.kernel_haven.incremental.util.FileUtil;
import net.ssehub.kernel_haven.incremental.util.Marshaller;
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
	 * @param file
	 *            the git diff file
	 * @throws IOException
	 */
	public DiffFile(@NonNull Collection<FileEntry> changeSet) throws IOException {
		this.changeSet = changeSet;
	}

	public Collection<FileEntry> getEntries() {
		return changeSet;
	}

	public void save(File file) throws IOException, JAXBException {
		if (file.exists()) {
			file.delete();
		}
		FileUtil.writeFile(file, Marshaller.marshalToJson(this, DiffFile.class));
	}

	public static DiffFile load(File file) throws JAXBException, IOException {
		String json = FileUtil.readFile(file);
		return (DiffFile) Marshaller.unmarshalFromJson(json, DiffFile.class);
	}

	/** Required for use with {@link Marshaller} */
	@SuppressWarnings("unused")
	private DiffFile() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changeSet == null) ? 0 : changeSet.hashCode());
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
		DiffFile other = (DiffFile) obj;
		if (changeSet == null) {
			if (other.changeSet != null)
				return false;
		} else if (!changeSet.containsAll(other.changeSet) && changeSet.size() != other.changeSet.size())
			return false;
		return true;
	}

}
