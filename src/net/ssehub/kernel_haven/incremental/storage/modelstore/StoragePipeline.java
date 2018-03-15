package net.ssehub.kernel_haven.incremental.storage.modelstore;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.analysis.PipelineAnalysis;
import net.ssehub.kernel_haven.config.Configuration;

/**
 * @author Moritz
 */
public class StoragePipeline extends PipelineAnalysis {

    public StoragePipeline(Configuration config) {
        super(config);
    }

    @Override
    protected AnalysisComponent<?> createPipeline() throws SetUpException {
        return new IncrementalPostExtraction(config, getCmComponent());
    }

}