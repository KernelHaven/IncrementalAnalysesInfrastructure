package net.ssehub.kernel_haven.incremental.diff;

import java.io.File;

public abstract class DiffApplier {
    
    public DiffApplier(File filesStorageDir, File inputDiff) {
    }
    
    protected DiffApplier() {
    }
    
    
    public abstract boolean mergeChanges();
    
    public abstract boolean revertChanges();

}
