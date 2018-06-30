package net.ssehub.kernel_haven.incremental.analysis;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.analysis.PipelineAnalysis;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.storage.IncrementalPostExtraction;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * 
 * An Analysis that really does nothing in terms of a real Analysis. This only runs
 * the parts of an incremental analysis that need to be run before any core analyis can
 * be started. However it does not perform any analysis on the models after those parts.
 * 
 * If you want to implement an incremental analysis, use the HybridCache that is the result
 * of the {@link IncrementalPostExtraction}
 * 
 * @author moritz
 */
public class DoNothingAnalysis extends PipelineAnalysis {

	/**
	 * Instantiates a new do nothing analysis.
	 *
	 * @param config the config
	 */
	public DoNothingAnalysis(@NonNull Configuration config) {
		super(config);

	}


	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.analysis.PipelineAnalysis#createPipeline()
	 */
	@Override
	protected AnalysisComponent<?> createPipeline() throws SetUpException {
		return new IncrementalPostExtraction(config, getCmComponent(), getBmComponent(), getVmComponent());

	}

}