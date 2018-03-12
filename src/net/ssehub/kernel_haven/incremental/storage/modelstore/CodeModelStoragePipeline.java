package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.util.Logger;

public class CodeModelStoragePipeline extends AnalysisComponent<Object> {

	private AnalysisComponent<SourceFile> sourceFiles;
	private static final Logger LOGGER = Logger.get();
	private Configuration config;


	public CodeModelStoragePipeline(Configuration config, AnalysisComponent<SourceFile> cmComponent) throws SetUpException {
		super(config);
		this.config = config;
		IncrementalAnalysisSettings.registerAllSettings(config);
		this.sourceFiles = cmComponent;
	}

	@Override
	protected void execute() {


		
		config.getValue(DefaultSettings.CODE_EXTRACTOR_FILES);
		config.setValue(DefaultSettings.CODE_EXTRACTOR_FILES, value);

		Map<File, SourceFile> changedFilesModel = new HashMap<File, SourceFile>();


		SourceFile file;
		while ((file = sourceFiles.getNextResult()) != null) {
			changedFilesModel.put(file.getPath(), file);
		}

		Collection<SourceFile> resultingAnalyisModel = new ArrayList<>();

	}

	@Override
	public String getResultName() {
		return "CodeModelStoragePipeline";
	}

}