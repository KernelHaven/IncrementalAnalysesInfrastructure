package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.diff.linecount.LineCounter;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparation;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.storage.HybridCache.ChangeFlag;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
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

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.get();

	/** The cm component. */
	private AnalysisComponent<SourceFile<?>> cmComponent;

	/** The config. */
	private Configuration config;

	/** The bm component. */
	private AnalysisComponent<BuildModel> bmComponent;

	/** The vm component. */
	private AnalysisComponent<VariabilityModel> vmComponent;

	/**
	 * Instantiates a new IncremenmtalPostExtraction.
	 *
	 * @param config            the config
	 * @param analysisComponent the cm component
	 * @param bmComponent       the bm component
	 * @param vmComponent       the vm component
	 * @throws SetUpException thrown if required parameters were not configured
	 *                        correctly.
	 */
	public IncrementalPostExtraction(Configuration config, @NonNull AnalysisComponent<SourceFile<?>> analysisComponent,
			AnalysisComponent<BuildModel> bmComponent, AnalysisComponent<VariabilityModel> vmComponent)
			throws SetUpException {
		super(config);
		this.config = config;
		IncrementalAnalysisSettings.registerAllSettings(config);
		this.cmComponent = analysisComponent;
		this.bmComponent = bmComponent;
		this.vmComponent = vmComponent;
	}

	/**
	 * Try join thread.
	 *
	 * @param thread the thread
	 */
	private void tryJoinThread(Thread thread) {
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				LOGGER.logException("Thread interrupted", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ssehub.kernel_haven.analysis.AnalysisComponent#execute()
	 */
	@Override
	protected void execute() {

		HybridCache hybridCache = new HybridCache(config.getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY));

		try {
			hybridCache.clearChangeHistory();
		} catch (IOException exc1) {
			LOGGER.logException("Could not clear history for HybridCache ", exc1);
		}

		// start threads for each model-type so they can run parallel
		Thread cmThread = null;
		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL)) {
			cmThread = new Thread() {
				public void run() {
					codeModelExtraction(hybridCache, config.getValue(IncrementalAnalysisSettings.DELETED_FILES));
				}
			};
			cmThread.start();
		}

		Thread vmThread = null;
		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL)) {
			vmThread = new Thread() {
				public void run() {
					variabilityModelExtraction(hybridCache);
				}
			};
			vmThread.start();
		}

		Thread bmThread = null;
		if (config.getValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL)) {
			bmThread = new Thread() {
				public void run() {
					buildModelExtraction(hybridCache);
				}
			};
			bmThread.start();
		}

		// wait for all model-threads to finish
		tryJoinThread(cmThread);
		tryJoinThread(vmThread);
		tryJoinThread(bmThread);

		// Update code line information for files that were not extracted but
		// have changed
		if (config.getValue(IncrementalAnalysisSettings.UPDATE_CODE_LINES)) {
			try {
				updateCodeLineInformation(
						DiffFileParser.parse(config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE)),
						hybridCache);
			} catch (IllegalArgumentException | IOException | FormatException exc) {
				LOGGER.logException("Could not update codelines for models", exc);
			}
		}

		this.addResult(hybridCache);
	}

	/**
	 * Update code line information.
	 *
	 * @param diffFile    the diff file
	 * @param hybridCache the hybrid cache
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException              Signals that an I/O exception has occurred.
	 * @throws FormatException          the format exception
	 */
	private void updateCodeLineInformation(DiffFile diffFile, HybridCache hybridCache)
			throws IllegalArgumentException, IOException, FormatException {

		// Create list of extracted paths as those are the paths that
		// do not need to be considered for line updates
		Collection<SourceFile<?>> extractedSourceFiles = hybridCache
				.readCm(hybridCache.getCmPathsForFlag(ChangeFlag.EXTRACTION_CHANGE));
		Collection<Path> extractedPaths = new ArrayList<>();
		extractedSourceFiles.forEach(srcFile -> extractedPaths.add(srcFile.getPath().toPath()));

		// Initialize a lineCounter that parses the line information in the
		// git diff file but skips the paths defined by extractedPaths for
		// extraction
		// to yield better performance
		LineCounter counter = new LineCounter(config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE));

		// iterate over all entries to the diff file
		for (FileEntry entry : diffFile.getEntries()) {
			// only update lines for entries that were modifications and
			// were not already covered by the extraction process.
			if (entry.getType().equals(FileEntry.FileChange.MODIFICATION)
					&& !extractedPaths.contains(entry.getPath())) {
				SourceFile<?> srcFile = hybridCache.readCm(entry.getPath().toFile());
				if (srcFile != null) {
					Logger.get().logDebug("Updating lines for file: " + entry.getPath());
					// Iterate over sourcefile and update line numbers
					Iterator<CodeElement<?>> itr = (Iterator<CodeElement<?>>) srcFile.iterator();

					while (itr.hasNext()) {
						CodeElement<?> element = itr.next();
						// recurively handle element and nested elements
						updateLineNumbersForElement(counter, element);
					}

					hybridCache.write(srcFile);
					hybridCache.flag(srcFile, ChangeFlag.AUXILLARY_CHANGE);
				}
			}
		}

	}

	/**
	 * Update line numbers for element.
	 *
	 * @param counter the counter
	 * @param element the element
	 */
	private void updateLineNumbersForElement(LineCounter counter, CodeElement<?> element) {
		int numberOfNestedElements = element.getNestedElementCount();
		for (int i = 0; i< numberOfNestedElements; i++) {
			CodeElement<?> nested = element.getNestedElement(i);
			updateLineNumbersForElement(counter, nested);
		}
		Path sourceFilePath = element.getSourceFile().toPath();

		int previousStart = element.getLineStart();
		int previousEnd = element.getLineEnd();

		if (previousStart >= 0) {
			int newStart = counter.getNewLineNumber(sourceFilePath, previousStart);
			element.setLineStart(newStart);
		}
		if (previousEnd >= 0) {
			int newEnd = counter.getNewLineNumber(sourceFilePath, previousEnd);
			element.setLineEnd(newEnd);
		}

	}

	/**
	 * Variability model extraction.
	 *
	 * @param hybridCache the hybrid cache to write the extracated results to.
	 */
	private void variabilityModelExtraction(HybridCache hybridCache) {
		VariabilityModel variabilityModel;
		// CHECKSTYLE:OFF
		if ((variabilityModel = vmComponent.getNextResult()) != null) {
			// CHECKSTYLE:ON
			try {
				hybridCache.write(variabilityModel);
				hybridCache.flagVariabilityModel(ChangeFlag.EXTRACTION_CHANGE);
			} catch (IOException e) {
				LOGGER.logException("Could not write variability-model to " + HybridCache.class.getSimpleName(), e);
			}
		} else {
			try {
				hybridCache.deleteVariabilityModel();
				hybridCache.flagVariabilityModel(ChangeFlag.EXTRACTION_CHANGE);
			} catch (IOException e) {
				LOGGER.logException("Could not delete variability-model from " + HybridCache.class.getSimpleName(), e);
			}
		}
	}

	/**
	 * Build model extraction.
	 *
	 * @param hybridCache the hybrid cache to write the extracated results to.
	 */
	private void buildModelExtraction(HybridCache hybridCache) {
		BuildModel buildModel;
		// CHECKSTYLE:OFF
		if ((buildModel = bmComponent.getNextResult()) != null) {
			// CHECKSTYLE:ON
			try {
				hybridCache.write(buildModel);
				hybridCache.flagBuildModel(ChangeFlag.EXTRACTION_CHANGE);
			} catch (IOException e) {
				LOGGER.logException("Could not write build-model to " + HybridCache.class.getSimpleName(), e);
			}
		} else {
			try {
				hybridCache.deleteBuildModel();
				hybridCache.flagBuildModel(ChangeFlag.EXTRACTION_CHANGE);
			} catch (IOException e) {
				LOGGER.logException("Could not delete build-model from " + HybridCache.class.getSimpleName(), e);
			}
		}
	}

	/**
	 * Code model extraction.
	 *
	 * @param hybridCache  the hybrid cache to write the extracted results to.
	 * @param deletedFiles the deleted files
	 */
	private void codeModelExtraction(HybridCache hybridCache, List<String> deletedFiles) {
		SourceFile file;

		// delete all models corresponding to deleted files
		for (String entry : deletedFiles) {
			try {
				LOGGER.logDebug("Deleting model because of " + FileEntry.class.getSimpleName() + entry);
				hybridCache.deleteCodeModel(new File(entry));
			} catch (IOException exception) {
				LOGGER.logException("Could not delete code model of file " + entry + ". "
						+ "This may result in an inconsistent state of " + HybridCache.class.getSimpleName() + ". "
						+ "To fix an inconsistent state you can either do a rollback "
						+ "or extract all models from scratch.", exception);
			}

		}

		// Add new models to hybridCache
		while ((file = cmComponent.getNextResult()) != null) {
			try {
				hybridCache.write(file);
				hybridCache.flag(file, ChangeFlag.EXTRACTION_CHANGE);
			} catch (IOException e) {
				LOGGER.logException("Could not write code model for file " + file.getPath().getPath() + " to "
						+ HybridCache.class.getSimpleName(), e);
			}
		}
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