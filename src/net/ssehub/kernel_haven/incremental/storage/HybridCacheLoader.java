package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

/**
 * This class is an {@link AnalysisComponent} which provides access to an
 * existing HybridCache. {@link HybridCacheLoader} is meant to enable the
 * repeated processing of one state of the {@link HybridCache}
 * 
 * @author moritz
 */
public class HybridCacheLoader extends AnalysisComponent<HybridCache> {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.get();

    /** The config. */
    private Configuration config;

    public HybridCacheLoader(Configuration config,
        AnalysisComponent<SourceFile> cmComponent,
        AnalysisComponent<BuildModel> bmComponent,
        AnalysisComponent<VariabilityModel> vmComponent) throws SetUpException {
        super(config);
        this.config = config;
        IncrementalAnalysisSettings.registerAllSettings(config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.ssehub.kernel_haven.analysis.AnalysisComponent#execute()
     */
    @Override
    protected void execute() {
        File cacheDir =
            config.getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY);
        LOGGER.logDebug(
            "Reusing existing hybrid Cache from directory " + cacheDir);
        HybridCache hybridCache = new HybridCache(cacheDir);

        this.addResult(hybridCache);
    }

    /**
     * Gets the result name.
     *
     * @return the result name
     */
    /*
     * (non-Javadoc)
     * 
     * @see net.ssehub.kernel_haven.analysis.AnalysisComponent#getResultName()
     */
    @Override
    public String getResultName() {
        return HybridCache.class.getSimpleName();
    }

}