package net.ssehub.kernel_haven.incremental.storage;

import java.io.IOException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparation;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.util.diff.FileEntry;
import net.ssehub.kernel_haven.incremental.util.diff.analyzer.SimpleDiffAnalyzer;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

/**
 * This class is an {@link AnalysisComponent} which handles the extraction of
 * models within an incremental analysis pipeline. It should be used in
 * conjunction with the preparation task {@link IncrementalPreparation} The
 * result is given as {@link HybridCache}.
 * 
 * @author moritz
 */
public class IncrementalPostExtraction extends AnalysisComponent<HybridCache> {

	/** The cm component. */
	private AnalysisComponent<SourceFile> cmComponent;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.get();

	/** The config. */
	private Configuration config;

	/** The bm component. */
	private AnalysisComponent<BuildModel> bmComponent;

	/** The vm component. */
	private AnalysisComponent<VariabilityModel> vmComponent;

	/**
	 * Instantiates a new IncremenmtalPostExtraction.
	 *
	 * @param config
	 *            the config
	 * @param cmComponent
	 *            the cm component
	 * @param bmComponent
	 *            the bm component
	 * @param vmComponent
	 *            the vm component
	 * @throws SetUpException
	 *             thrown if required parameters were not configured correctly.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ssehub.kernel_haven.analysis.AnalysisComponent#execute()
	 */
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

	/**
	 * Variability model extraction.
	 *
	 * @param hybridCache
	 *            the hybrid cache to write the extracated results to.
	 */
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

	/**
	 * Builds the model extraction.
	 *
	 * @param hybridCache
	 *            the hybrid cache to write the extracated results to.
	 */
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

	/**
	 * Code model extraction.
	 *
	 * @param hybridCache
	 *            the hybrid cache to write the extracated results to.
	 */
	private void codeModelExtraction(HybridCache hybridCache) {
		SourceFile file;
		DiffFile diffFile;
		try {
			diffFile = new DiffFile(
					new SimpleDiffAnalyzer(config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE)));
			for (FileEntry entry : diffFile.getEntries()) {
				if (entry.getType().equals(FileEntry.Type.DELETION)) {
					try {
						hybridCache.deleteCodeModel(entry.getPath().toFile());
					} catch (IOException exception) {
						LOGGER.logException("Could not delete CodeModel-File. "
								+ "This may result in an inconsistent state of Hybridcache. "
								+ "To fix an inconsistent state you can either do a rollback "
								+ "or extract all models from scratch.", exception);
					}
				}
			}
		} catch (IOException e) {
			//Should not happen but if it does, we want to know
			LOGGER.logException(
					"DiffFile \"" + config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE).getAbsolutePath()
							+ "\" could not be accessed eventhough it got approved when registerAllSettings() was called.",
					e);
		}

		while ((file = cmComponent.getNextResult()) != null) {
			try {
				hybridCache.write(file);
			} catch (IOException e) {
				LOGGER.logException("Could not write sourcefile to HybridCache", e);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ssehub.kernel_haven.analysis.AnalysisComponent#getResultName()
	 */
	@Override
	public String getResultName() {
		return "HybridCache";
	}

}