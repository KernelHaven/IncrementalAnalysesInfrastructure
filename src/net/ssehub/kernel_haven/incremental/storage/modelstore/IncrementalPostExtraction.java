package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

public class IncrementalPostExtraction extends AnalysisComponent<HybridCache> {

	private AnalysisComponent<SourceFile> cmComponent;
	private static final Logger LOGGER = Logger.get();
	private Configuration config;
	private AnalysisComponent<BuildModel> bmComponent;
	private AnalysisComponent<VariabilityModel> vmComponent;

	public IncrementalPostExtraction(Configuration config, AnalysisComponent<SourceFile> cmComponent,
			AnalysisComponent<BuildModel> bmComponent, AnalysisComponent<VariabilityModel> vmComponent)
			throws SetUpException {
		super(config);
		this.config = config;
		IncrementalAnalysisSettings.registerAllSettings(config);
		this.cmComponent = cmComponent;
		this.bmComponent = bmComponent;
		this.vmComponent = vmComponent;
	}

	@Override
	protected void execute() {

		HybridCache hybridCache = new HybridCache(config.getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY));
		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL)) {
			codeModelExtraction(hybridCache);
		} else

		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL)) {
			variabilityModelExtraction(hybridCache);
		}

		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL)) {
			buildModelExtraction(hybridCache);
		}

		this.addResult(hybridCache);

	}

	private void variabilityModelExtraction(HybridCache hybridCache) {
		VariabilityModel variabilityModel;
		while ((variabilityModel = vmComponent.getNextResult()) != null) {
			try {
				hybridCache.write(variabilityModel);
			} catch (IOException e) {
				LOGGER.logException("Could not write variability-model to HybridCache", e);
			}
		}
	}

	private void buildModelExtraction(HybridCache hybridCache) {
		BuildModel buildModel;
		while ((buildModel = bmComponent.getNextResult()) != null) {
			try {
				hybridCache.write(buildModel);
			} catch (IOException e) {
				LOGGER.logException("Could not write build-model to HybridCache", e);
			}
		}
	}

	private void codeModelExtraction(HybridCache hybridCache) {
		SourceFile file;
		DiffFile diffFile;
		try {
			diffFile = new DiffFile(config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE));
			for (Path deleted : diffFile.getDeleted()) {
				try {
					hybridCache.deleteCodeModel(deleted.toFile());
				} catch (IOException exception) {
					LOGGER.logException("Could not delete CodeModel-File. "
							+ "This may result in an inconsistent state of Hybridcache. "
							+ "To fix an inconsistent state you can either do a rollback "
							+ "or extract all models from scratch.", exception);
				}
			}
		} catch ( IllegalArgumentException | IOException e) {
			// Does not happen as the existence of the file is already checked when through
			// IncrementalAnalysisSettings.registerAllSettings(config);
			LOGGER.logException("DiffFile was not found in method eventhough it got approved when "
					+ "registerAllSettings was called.", e);
		}

		while ((file = cmComponent.getNextResult()) != null) {
			try {
				hybridCache.write(file);
			} catch (IOException e) {
				LOGGER.logException("Could not write sourcefile to HybridCache", e);
			}
		}

	}

	@Override
	public String getResultName() {
		return "HybridCache";
	}

}