package net.ssehub.kernel_haven.incremental.analysis;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.analysis.PipelineAnalysis;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.storage.HybridCacheAdapter;
import net.ssehub.kernel_haven.incremental.storage.IncrementalPostExtraction;
import net.ssehub.kernel_haven.undead_analyzer.DeadCodeAnalysis;
import net.ssehub.kernel_haven.undead_analyzer.DeadCodeFinder;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Incremental implementation of the {@link DeadCodeAnalysis}
 *
 * @author Moritz
 */
public class IncrementalDeadCodeAnalysis extends PipelineAnalysis {
	
	Logger LOGGER = Logger.get();

	/**
	 * Instantiates a new incremental dead code analysis.
	 *
	 * @param config the config
	 */
	public IncrementalDeadCodeAnalysis(Configuration config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.analysis.PipelineAnalysis#createPipeline()
	 */
	@Override
	protected AnalysisComponent<?> createPipeline() throws SetUpException {
		
		HybridCacheAdapter hca = new HybridCacheAdapter(config,
				new IncrementalPostExtraction(config, getCmComponent(), getBmComponent(), getVmComponent()), true);

		LOGGER.logInfo("Starting Dead Code Finder");
		
		DeadCodeFinder dcf = new DeadCodeFinder(config, hca.getVmComponent(), hca.getBmComponent(),
				hca.getCmComponent());

		LOGGER.logInfo("Dead Code Finder done.");
		
		return dcf;

	}

}