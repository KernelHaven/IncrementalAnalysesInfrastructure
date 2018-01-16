package net.ssehub.kernel_haven.incremental.common;

import java.io.File;

import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings.VersioningType;

/**
 * A configuration for the ModelStoragePipeline .
 * 
 * @author Moritz
 */
public class Configuration {

    public File getSourceDir() {
		return new File("./file");
    }
    
    public File getModelDir() {
    		return new File("./model");
    }
    
    public String getModelTagForAnalysis() {
    		return "tag-for-analysis";
    }
    
    public String getModelTagForReference() {
    	return "tag-for-reference";
    }
    
    public VersioningType getVersioningType() {
    		return VersioningType.FLAT;
    }
    
    
}
