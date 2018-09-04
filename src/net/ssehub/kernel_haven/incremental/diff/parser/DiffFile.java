package net.ssehub.kernel_haven.incremental.diff.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * This class represents changes extracted from a diff-file.
 * 
 * @author Moritz
 */
public class DiffFile {

    /** The diff. */
    private Map<Path, FileEntry> changeSet = new LinkedHashMap<>();

    /**
     * Instantiates a new diff file reader. The file passed to this constructor must
     * be a git diff file.
     *
     * @param fileEntries the file entries
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public DiffFile(@NonNull Collection<FileEntry> fileEntries) throws IOException {
        for (FileEntry entry : fileEntries) {
            this.changeSet.put(entry.getPath(), entry);
        }
    }

    /**
     * Gets the entries.
     *
     * @return the entries
     */
    public Collection<FileEntry> getEntries() {

        return this.changeSet.values();
    }

    /**
     * Gets the entry.
     *
     * @param file the file
     * @return the entry
     */
    public FileEntry getEntry(Path file) {
        return this.changeSet.get(file);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changeSet == null) ? 0 : changeSet.hashCode());
        return result;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DiffFile other = (DiffFile) obj;
        if (changeSet == null) {
            if (other.changeSet != null) {
                return false;
            }
        } else if (!changeSet.equals(other.changeSet)) {
            return false;
        }
        return true;
    }

}
