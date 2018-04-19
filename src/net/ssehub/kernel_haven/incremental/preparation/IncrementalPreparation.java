package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.preparation.filter.InputFilter;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.util.diff.DiffApplyUtil;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Preparation task for incremental analyses. This class is used to integrate a
 * diff on the filebase of the source tree and subsequently select a subset of
 * the resulting files for extraction and analyses.
 * {@link IncrementalPreparation} must be used as preparation when working with
 * an incremental analysis.
 * 
 * @author moritz
 */
public class IncrementalPreparation implements IPreparation {

	/** Logger instance. */
	private static final Logger LOGGER = Logger.get();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ssehub.kernel_haven.IPreparation#run(net.ssehub.kernel_haven.config.
	 * Configuration)
	 */
	@Override
	public void run(Configuration config) throws SetUpException {
		IncrementalAnalysisSettings.registerAllSettings(config);
		LOGGER.logInfo("IncrementalPreparation started");

		File inputDiff = (File) config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
		File inputSourceDir = (File) config.getValue(DefaultSettings.SOURCE_TREE);

		// Merge changes
		DiffApplyUtil mergeUtil = new DiffApplyUtil(inputSourceDir, inputDiff);
		boolean mergeSuccessful = mergeUtil.mergeChanges();

		// only continue if merge was successful
		if (!mergeSuccessful) {
			LOGGER.logError("Could not merge provided diff with existing input files!\n"
					+ "The diff-file must describe changes that can be applied to the set of input-files that are to be analyzed. \n"
					+ "Stopping execution of KernelHaven.");
			throw new SetUpException("Could not merge provided diff with existing input files!");
		} else {
			DiffFile diffFile = generateDiffFile(config.getValue(IncrementalAnalysisSettings.DIFF_ANALYZER_CLASS_NAME),
					inputDiff);
			try {
				try {
					diffFile.save(new File(inputDiff.getAbsolutePath() + ".parsed"));
				} catch (JAXBException e) {
					LOGGER.logDebug("Could not store parsed version of DiffFile."
							+ " A complete parse will be performed when access to the diff-file"
							+ " is needed after model extraction.");
				}
			} catch (IOException e) {
				throw new SetUpException("Could not analyze Diff File", e);
			}

			//////////////////////////
			// Filter for codemodel //
			//////////////////////////
			Collection<Path> filteredPaths = filterInput(
					config.getValue(IncrementalAnalysisSettings.CODE_MODEL_FILTER_CLASS), inputSourceDir, diffFile,
					config.getValue(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX));

			if (!filteredPaths.isEmpty()) {
				config.setValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL, true);
				ArrayList<String> pathStrings = new ArrayList<String>();
				filteredPaths.forEach(path -> pathStrings.add(path.toString()));
				config.setValue(DefaultSettings.CODE_EXTRACTOR_FILES, pathStrings);
				// If no paths are included after filtering, the extraction does not need to run
			} else {
				config.setValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL, false);
			}

			//////////////////////////////////
			// Filter for variability model //
			//////////////////////////////////
			filteredPaths = filterInput(config.getValue(IncrementalAnalysisSettings.VARIABILITY_MODEL_FILTER_CLASS),
					inputSourceDir, diffFile, config.getValue(DefaultSettings.VARIABILITY_EXTRACTOR_FILE_REGEX));
			config.setValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL, !filteredPaths.isEmpty());

			////////////////////////////
			// Filter for build model //
			////////////////////////////
			filteredPaths = filterInput(config.getValue(IncrementalAnalysisSettings.BUILD_MODEL_FILTER_CLASS),
					inputSourceDir, diffFile, config.getValue(DefaultSettings.BUILD_EXTRACTOR_FILE_REGEX));
			config.setValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL, !filteredPaths.isEmpty());

		}

		// Finish and let KernelHaven run
		LOGGER.logInfo("IncrementalPreparation finished");
	}

	/**
	 * Filters input using the class defined by filterClassName. This should be a
	 * class available in the classpath and implementing InputFilter.
	 *
	 * @param filterClassName
	 *            the filter class name
	 * @param inputSourceDir
	 *            the input source dir
	 * @param inputDiff
	 *            the input diff
	 * @param regex
	 *            the regex
	 * @return the collection
	 * @throws SetUpException
	 *             the set up exception
	 */
	@SuppressWarnings("unchecked")
	protected Collection<Path> filterInput(String filterClassName, File inputSourceDir, DiffFile inputDiff,
			Pattern regex) throws SetUpException {
		Collection<Path> paths = null;
		// Call the method getFilteredResult for filterClassName via reflection-api
		try {
			@SuppressWarnings("rawtypes")
			Class filterClass = Class.forName(filterClassName);
			Object filterObject = filterClass.getConstructor(File.class, DiffFile.class, Pattern.class)
					.newInstance(inputSourceDir, inputDiff, regex);
			if (filterObject instanceof InputFilter) {
				Method getFilteredResultMethod = filterClass.getMethod("getFilteredResult");
				paths = (Collection<Path>) getFilteredResultMethod.invoke(filterObject);
			} else {
				throw new SetUpException(
						"The class name provided for the filter does not appear to extend the InputFilter class");
			}

		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
				| InvocationTargetException e) {
			LOGGER.logException("The specified filter class could not be used", e);
			throw new SetUpException("The specified filter could not be used: " + e.getMessage());
		}
		return paths;

	}

	/**
	 * Filters input using the class defined by filterClassName. This should be a
	 * class available in the classpath and implementing InputFilter.
	 *
	 * @param filterClassName
	 *            the filter class name
	 * @param inputSourceDir
	 *            the input source dir
	 * @param inputDiff
	 *            the input diff
	 * @param regex
	 *            the regex
	 * @return the collection
	 * @throws SetUpException
	 *             the set up exception
	 */
	@SuppressWarnings("unchecked")
	protected DiffFile generateDiffFile(String analyzerClassName, File inputGitDiff) throws SetUpException {
		DiffFile diffFile = null;
		// Call the method getFilteredResult for filterClassName via reflection-api
		try {
			@SuppressWarnings("rawtypes")
			Class analyzerClass = Class.forName(analyzerClassName);
			Method getFilteredResultMethod = analyzerClass.getMethod("generateDiffFile", File.class);
			LOGGER.logInfo("Analyzing git-diff with " + analyzerClass.getSimpleName()
					+ ". This may take a while for large git-diffs.");
			diffFile = (DiffFile) getFilteredResultMethod.invoke(null, inputGitDiff);

		} catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException
				| InvocationTargetException e) {
			throw new SetUpException("The specified DiffAnalyzer class \"" + analyzerClassName
					+ "\" could not be used: " + e.getClass().getName() + "\n" + e.getMessage());
		}
		return diffFile;

	}

}
