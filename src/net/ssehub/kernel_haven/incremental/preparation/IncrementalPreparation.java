package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.common.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.util.DiffIntegrationUtil;
import net.ssehub.kernel_haven.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class StoragePreparation.
 */
public class IncrementalPreparation implements IPreparation {

	/** The Constant LOGGER. */
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

		File inputDiff = (File) config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
		File inputSourceDir = (File) config.getValue(DefaultSettings.SOURCE_TREE);

		// Merge changes
		DiffIntegrationUtil mergeUtil = new DiffIntegrationUtil(inputSourceDir, inputDiff);
		boolean mergeSuccessful = mergeUtil.mergeChanges();

		// only continue if merge was successful
		if (!mergeSuccessful) {
			LOGGER.logError("Could not merge provided diff with existing input files!\n"
					+ "The diff-file must describe changes that can be applied to the set of input-files that are to be analyzed. \n"
					+ "Stopping execution of KernelHaven.");
			throw new SetUpException("Could not merge provided diff with existing input files!");
		} else {
			// Filter input
			filterInput(config.getValue(IncrementalAnalysisSettings.FILTER_CLASS), inputSourceDir, inputDiff, config);
		}

		// Finish and let KernelHaven run
	}

	/**
	 * Filters the input and modifies the configuration to only consider the result
	 * of the filtering for extraction.
	 *
	 * @param filterClassName
	 *            the filter class name
	 * @param inputSourceDir
	 *            the input source dir
	 * @param inputDiff
	 *            the input diff
	 * @param config
	 *            the config
	 * @throws SetUpException
	 *             the set up exception
	 */
	@SuppressWarnings("unchecked")
	private void filterInput(String filterClassName, File inputSourceDir, File inputDiff, Configuration config)
			throws SetUpException {
		Collection<Path> paths = null;
		try {
			@SuppressWarnings("rawtypes")
			Class filterClass = Class.forName(filterClassName);
			Object filterObject = filterClass.getConstructor(File.class, File.class).newInstance(inputSourceDir,
					inputDiff);
			if (filterObject instanceof InputFilter) {
				Method getFilteredResultMethod = filterClass.getMethod("getFilteredResult");
				paths = (Collection<Path>) getFilteredResultMethod.invoke(filterObject, inputSourceDir, inputDiff);
			} else {
				throw new SetUpException(
						"The class name provided for the filter does not appear to extend the InputFilter class");
			}

		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
				| InvocationTargetException e) {
			LOGGER.logException("The specified filter class could not be used", e);
			throw new SetUpException("The specified filter could not be used: " + e.getMessage());
		}

		ArrayList<String> pathStrings = new ArrayList<String>();
		paths.forEach(path -> pathStrings.add(path.toString()));
		config.setValue(DefaultSettings.CODE_EXTRACTOR_FILES, pathStrings);

	}

}
