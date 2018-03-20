package net.ssehub.kernel_haven.incremental.storage.modelstore;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.analysis.PipelineAnalysis;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.undead_analyzer.DeadCodeFinder;

/**
 * @author Moritz
 */
public class StoragePipeline extends PipelineAnalysis {

	public StoragePipeline(Configuration config) {
		super(config);
	}

	@Override
	protected AnalysisComponent<?> createPipeline() throws SetUpException {
		HybridCacheAdapter hca = new HybridCacheAdapter(config,
				new IncrementalPostExtraction(config, getCmComponent(), getBmComponent(), getVmComponent()));

		DeadCodeFinder dcf = new DeadCodeFinder(config, hca.getVmComponent(), hca.getBmComponent(),
				hca.getCmComponent());

		return dcf;

	}

}