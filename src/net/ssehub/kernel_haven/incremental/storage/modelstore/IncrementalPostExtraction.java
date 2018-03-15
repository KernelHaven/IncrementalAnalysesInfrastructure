package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.IOException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.util.Logger;

public class IncrementalPostExtraction extends AnalysisComponent<Object> {

	private AnalysisComponent<SourceFile> sourceFiles;
	private static final Logger LOGGER = Logger.get();
	private Configuration config;

	public IncrementalPostExtraction(Configuration config, AnalysisComponent<SourceFile> cmComponent)
			throws SetUpException {
		super(config);
		this.config = config;
		IncrementalAnalysisSettings.registerAllSettings(config);
		this.sourceFiles = cmComponent;
	}

	@Override
	protected void execute() {
		HybridCache hybridCache = new HybridCache(config.getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY));
		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL)) {
			codeModelExtraction(hybridCache);
		}

		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL)) {
			variabilityModelExtraction(hybridCache);
		}

		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL)) {
			buildModelExtraction(hybridCache);
		}

	}

	private void variabilityModelExtraction(HybridCache hybridCache) {
		// TODO Auto-generated method stub

	}

	private void codeModelExtraction(HybridCache hybridCache) {
		SourceFile file;
		while ((file = sourceFiles.getNextResult()) != null) {
			try {
				hybridCache.write(file);
			} catch (IOException e) {
				LOGGER.logException("Could not write sourcefile to HybridCache", e);
			}
		}

	}

	private void buildModelExtraction(HybridCache hybridCache) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getResultName() {
		return "IncrementalPostExtraction";
	}

}