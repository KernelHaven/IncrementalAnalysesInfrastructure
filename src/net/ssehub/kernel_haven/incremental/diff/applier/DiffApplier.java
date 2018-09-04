package net.ssehub.kernel_haven.incremental.diff.applier;

/**
 * A {@link net.ssehub.kernel_haven.incremental.diff.applier.DiffApplier} is a
 * class that can apply the changes described by a {@link DiffFile} to a given
 * directory. A {@link net.ssehub.kernel_haven.incremental.diff.parser.DiffFile}
 * itself can be created by using
 * {@link net.ssehub.kernel_haven.incremental.diff.DiffFileParser}
 */
public interface DiffApplier {

    /**
     * Merge changes.
     *
     * @return true, if successful
     */
    public abstract boolean mergeChanges();

    /**
     * Revert changes.
     *
     * @return true, if successful
     */
    public abstract boolean revertChanges();

}
