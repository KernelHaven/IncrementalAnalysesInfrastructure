package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.util.Logger;

public class CodeModelStoragePipeline extends AnalysisComponent<Object> {

	private AnalysisComponent<SourceFile> sourceFiles;
	private static final Logger LOGGER = Logger.get();

	public static enum MergeStrategy {
		FILE_TO_FILE, EXTRACTED_ONLY
	}

	public CodeModelStoragePipeline(Configuration config, AnalysisComponent<SourceFile> cmComponent) {
		super(config);
		this.sourceFiles = cmComponent;
	}

	@Override
	protected void execute() {

		// TODO: read storage directory from settings/properties
		File codeModelStorageDir = new File("./storage/");

		// TODO: compare with old revision
		String referenceRevision = "tag1";

		// TODO: read analysisRevision tag from settings/properties
		String analysisRevision = "tag2";

		// TODO: get sourceFileRoot tag from settings/properties
		File sourceFileRoot = new File("./source-file-root/");

		// TODO: read mergeStrategy from settings/properties
		MergeStrategy mergeStrategy = MergeStrategy.FILE_TO_FILE;

		Map<File, SourceFile> changedFilesModel = new HashMap<File, SourceFile>();
		codeModelStorageDir.mkdirs();

		FlatFileCodeModelStorageProvider storage = new FlatFileCodeModelStorageProvider(codeModelStorageDir);

		// Get all newly extracted source files from the extractor
		SourceFile file;
		while ((file = sourceFiles.getNextResult()) != null) {
			changedFilesModel.put(file.getPath(), file);
		}

		Collection<SourceFile> resultingAnalyisModel = new ArrayList<>();

		/*
		 * If only a subset of all source files of the revision used for analysis has
		 * been used for extraction, the result can be merged with the previous result
		 * on a file by file basis. This only makes sense if the extractor operates in a
		 * way so that each code-source-file only results in and has effects on exactly
		 * one SourceFile-Object
		 */
		if (mergeStrategy.equals(MergeStrategy.FILE_TO_FILE)) {
			try {

				// we start off with the reference revision and will adjust it gradually to
				// represent the model for the current analysis
				Map<File, SourceFile> resultingAnalyisModelMap = storage.getModelAsMap(referenceRevision);

				//////////////////////////
				// Handle deleted files //
				//////////////////////////

				FlatFileSourceCodeProvider analysisRevisionSourceCodeProvider = new FlatFileSourceCodeProvider(
						sourceFileRoot, analysisRevision);

				// remove every source-file-Object of the reference revisions where the source
				// file has been removed in the analysis revision
				for (Map.Entry<File, SourceFile> entry : resultingAnalyisModelMap.entrySet()) {
					File sourceCodeFile = entry.getKey();
					if (analysisRevisionSourceCodeProvider.hasFile(sourceCodeFile)) {
						resultingAnalyisModelMap.remove(entry.getKey());
					}
				}

				//////////////////////////
				// Handle changed files //
				//////////////////////////

				// replace every SourceFile-Object whenever there is a newly extracted
				// SourceFile-Object is available
				for (Map.Entry<File, SourceFile> entry : changedFilesModel.entrySet()) {
					resultingAnalyisModelMap.put(entry.getKey(), entry.getValue());
				}

				// store the result
				resultingAnalyisModel.addAll(resultingAnalyisModelMap.values());
				storage.storeModelForTag(resultingAnalyisModel, analysisRevision);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.logException("Could not merge models.", e);
			}

		} else if (mergeStrategy.equals(MergeStrategy.EXTRACTED_ONLY)) {
			// Store the newly extracted model without comparison to any other model.
			// This may for example be used for storing and analyzing the initial commit.
			resultingAnalyisModel.addAll(changedFilesModel.values());
			try {
				storage.storeModelForTag(resultingAnalyisModel, analysisRevision);
			} catch (IOException e) {
				LOGGER.logException("Could not store model for initial commit", e);
			}
		}

		// Pass a set of Models

	}

	@Override
	public String getResultName() {
		return "CodeModelStoragePipeline";
	}

}