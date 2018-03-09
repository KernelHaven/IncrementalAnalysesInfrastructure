package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.util.DiffIntegrationUtil;
import net.ssehub.kernel_haven.util.Logger;

/**
 * The Class StoragePreparation.
 */
public class IncrementalPreparation implements IPreparation {

	private static final Logger LOGGER = Logger.get();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ssehub.kernel_haven.IPreparation#run(net.ssehub.kernel_haven.config.
	 * Configuration)
	 */
	@Override
	public void run(Configuration config) throws SetUpException {

		IncrementalAnalysisSettings.registerAllSettings(config);

		File filesStorageDir = (File) config.getValue(IncrementalAnalysisSettings.FILES_STORAGE_DIR);
		File inputDiff = (File) config.getValue(IncrementalAnalysisSettings.INPUT_DIFF_FILE);


		DiffIntegrationUtil mergeUtil = new DiffIntegrationUtil(filesStorageDir, inputDiff);
		mergeUtil.mergeChanges();

		// determine changes and new files in analysisRevision-files


	}

}
