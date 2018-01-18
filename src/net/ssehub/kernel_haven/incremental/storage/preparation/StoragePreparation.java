package net.ssehub.kernel_haven.incremental.storage.preparation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.storage.modelstore.FlatFileSourceCodeProvider;
import net.ssehub.kernel_haven.util.Logger;

/**
 * The Class StoragePreparation.
 */
public class StoragePreparation implements IPreparation {

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

		File sourceDir = config.getValue(IncrementalAnalysisSettings.SOURCE_DIR);
		String analysisRevision = config.getValue(IncrementalAnalysisSettings.MODEL_REVISION_FOR_ANALYSIS);
		String referenceRevision = config.getValue(IncrementalAnalysisSettings.MODEL_REVISION_FOR_ANALYSIS);

		FlatFileSourceCodeProvider sourceCodeProvider = new FlatFileSourceCodeProvider(sourceDir, analysisRevision);

		// determine changes and new files in analysisRevision-files
		Collection<File> changedFiles;
		try {
			changedFiles = sourceCodeProvider.getChangedOrAddedFiles(referenceRevision);
		} catch (IOException e) {
			LOGGER.logException("Could not generate changelist of analyis-folder compared to reference-folder."
					+ "\nAssuming every file of the analysis-folder as new or changed file.\n"
					+ "Therefore extraction will be done from scratch.", e);
			changedFiles = sourceCodeProvider.getAllFiles();
		}

		// TODO: get directory for extractor and extract to this location. The extractor
		// will take the changed files as input.
		File extractorTargetDir = null;
		for (File changedFile : changedFiles) {
			try {
				sourceCodeProvider.writeFileToFilesystem(changedFile, extractorTargetDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// - revision information will later also be used by Storage in the
		// AnalysisPipeline to tag the models in the ModelStorage

	}

}
