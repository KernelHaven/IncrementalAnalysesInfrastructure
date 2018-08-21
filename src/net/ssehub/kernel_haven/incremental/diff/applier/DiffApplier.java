package net.ssehub.kernel_haven.incremental.diff.applier;

// TODO: Auto-generated Javadoc
/**
 * The Class DiffApplier.
 */
public abstract class DiffApplier {
    

    /**
     * Instantiates a new diff applier.
     */
    protected DiffApplier() {
    }
    
    
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
