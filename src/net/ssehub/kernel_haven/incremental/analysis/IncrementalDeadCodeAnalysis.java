package net.ssehub.kernel_haven.incremental.analysis;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.analysis.PipelineAnalysis;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.storage.HybridCacheAdapter;
import net.ssehub.kernel_haven.incremental.storage.IncrementalPostExtraction;
import net.ssehub.kernel_haven.undead_analyzer.DeadCodeFinder;

/**
 * The Class IncrementalDeadCodeAnalysis.
 *
 * @author Moritz
 */
public class IncrementalDeadCodeAnalysis extends PipelineAnalysis {

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

		DeadCodeFinder dcf = new DeadCodeFinder(config, hca.getVmComponent(), hca.getBmComponent(),
				hca.getCmComponent());

		return dcf;

	}

}