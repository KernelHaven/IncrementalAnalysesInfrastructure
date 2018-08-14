package net.ssehub.kernel_haven.incremental.diff.applier;

import java.io.File;

public abstract class DiffApplier {
    

    protected DiffApplier() {
    }
    
    
    public abstract boolean mergeChanges();
    
    public abstract boolean revertChanges();

}
