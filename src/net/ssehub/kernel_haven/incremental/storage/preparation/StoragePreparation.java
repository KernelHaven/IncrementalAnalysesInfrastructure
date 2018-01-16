package net.ssehub.kernel_haven.incremental.storage.preparation;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;

// TODO: Auto-generated Javadoc
/**
 * The Class StoragePreparation.
 */
public class StoragePreparation implements IPreparation {

	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.IPreparation#run(net.ssehub.kernel_haven.config.Configuration)
	 */
	@Override
	public void run(Configuration config) throws SetUpException {
		
		// TODO:
		// ( - optional: pull current revision)
		// - access reference revision
		// - access current revision
		
		
		
		// - determine changes between revisions
		// - define extraction targets depending on what changed
		
		// should support git for storage space but be open to extension from other sources aswell
		
		
		// - revision information will later also be used by Storage in the AnalysisPipeline to tag the models in the ModelStorage
		
		
		
	}

}
