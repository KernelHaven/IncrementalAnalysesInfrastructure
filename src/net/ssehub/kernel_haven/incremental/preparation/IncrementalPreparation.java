package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import diff.DiffAnalyzer;
import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.diff.applier.DiffApplier;
import net.ssehub.kernel_haven.incremental.diff.applier.FileReplacingDiffApplier;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.preparation.filter.InputFilter;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.storage.HybridCache;
import net.ssehub.kernel_haven.incremental.util.FileUtil;
import net.ssehub.kernel_haven.util.Logger;

// TODO: Auto-generated Javadoc
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

    /**
     * Handle rollback.
     *
     * @param gitApplyUtil
     *            the git apply util
     * @param config
     *            the config
     */
    private void handleRollback(DiffApplier gitApplyUtil,
        Configuration config) {

        // Handle rollback
        boolean revertSuccessful = gitApplyUtil.revertChanges();
        HybridCache hybridCache = new HybridCache((File) config
            .getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY));
        try {
            hybridCache.rollback();
        } catch (IOException e) {
            revertSuccessful = false;
            LOGGER.logException("Could not revert changes in HybridCache.", e);
        }

        // Stop execution after rollback
        if (revertSuccessful) {
            LOGGER.logInfo("Rollback successful.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exc) {
                // Never happens
            }
            System.exit(0);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exc) {
                // Never happens
            }
            System.exit(1);
        }
    }

    /**
     * Run.
     *
     * @param config the config
     * @throws SetUpException the set up exception
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * net.ssehub.kernel_haven.IPreparation#run(net.ssehub.kernel_haven.config.
     * Configuration)
     */
    @Override
    public void run(Configuration config) throws SetUpException {
        long start = System.nanoTime();

        IncrementalAnalysisSettings.registerAllSettings(config);

        File inputDiff = (File) config
            .getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
        File inputSourceDir =
            (File) config.getValue(DefaultSettings.SOURCE_TREE);

        DiffFile diffFile = readDiffFile(inputDiff);
        DiffApplier diffApplier = null;
        if (diffFile != null) {
            diffApplier =
                new FileReplacingDiffApplier(inputSourceDir, diffFile);
        } else {
            LOGGER.logError("Diff file " + inputDiff.getPath()
                + " could not be read! Perhaps file is not a valid git-diff file.");
            throw new SetUpException(
                "Diff file could not be read! Perhaps file is not a valid git-diff file.");
        }

        // If this is a rollback execution, only a rollback and nothing more
        // will be done
        if (config.getValue(IncrementalAnalysisSettings.ROLLBACK)) {
            // Execution will stop after rollback is complete
            handleRollback(diffApplier, config);

            // If it is a normal execution, we continue our preparation for the
            // execution of extractory and analysis.
        } else {
            // Merge changes
            boolean mergeSuccessful = diffApplier.mergeChanges();
            // only continue if merge was successful
            if (!mergeSuccessful) {
                LOGGER.logError(
                    "Could not merge provided diff with existing input files!\n"
                        + "The diff-file must describe changes that can"
                        + " be applied to the set of input-files that are to be analyzed. \n"
                        + "Stopping execution of KernelHaven.");
                throw new SetUpException(
                    "Could not merge provided diff with existing input files!");
            } else {
                DiffFile diffFileForFiltering = generateDiffFile(
                    config.getValue(
                        IncrementalAnalysisSettings.DIFF_ANALYZER_CLASS_NAME),
                    inputDiff);

                defineTargetsForExtraction(config, inputSourceDir,
                    diffFileForFiltering);

                // Overwrite setting to preemptively start extractors as
                // extractors are only started when models need to be extracted.
                config.setValue(
                    DefaultSettings.ANALYSIS_PIPELINE_START_EXTRACTORS, false);

            }
        }

        long totalTime = System.nanoTime() - start;
        // Finish and let KernelHaven run
        LOGGER.logDebug(this.getClass().getSimpleName() + " duration:"
            + TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS)
            + "ms");
    }

    /**
     * Read diff file.
     *
     * @param inputDiff the input diff
     * @return the diff file
     * @throws SetUpException the set up exception
     */
    private DiffFile readDiffFile(File inputDiff)
        throws SetUpException {
        // Check if git diff file is empty. If an exception is thrown while
        // accessing the file, we also handle that here.
        boolean emptyFile = false;
        try {
            emptyFile = FileUtil.isEmptyFile(inputDiff);
        } catch (IOException exc) {
            LOGGER.logException(
                "Could not access file " + inputDiff.getPath() + "!", exc);
            throw new SetUpException(
                "Diff file could not be read! Make sure you have read access to the file.");
        }

        // Try to initialize a diff-applier with the parsed version of the diff
        // file.
        DiffFile diffFile = null;
        if (!emptyFile) {
            diffFile = DiffFileParser.parse(inputDiff);

        } else {
            LOGGER.logError("Diff file " + inputDiff.getPath()
                + " is empty! No new changes...");
            throw new SetUpException(
                "Stopping execution as diff file was empty.");
        }
        return diffFile;
    }

    /**
     * Define targets for extraction.
     *
     * @param config the config
     * @param inputSourceDir the input source dir
     * @param diffFileForFiltering the diff file for filtering
     * @throws SetUpException the set up exception
     */
    private void defineTargetsForExtraction(Configuration config,
        File inputSourceDir, DiffFile diffFileForFiltering)
        throws SetUpException {
        // Filter code model files
        Collection<Path> filteredPaths = filterInput(
            config
                .getValue(IncrementalAnalysisSettings.CODE_MODEL_FILTER_CLASS),
            inputSourceDir, diffFileForFiltering,
            config.getValue(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX), false);

        boolean extractCm = false;
        if (!filteredPaths.isEmpty()) {
            extractCm = true;
            ArrayList<String> pathStrings = new ArrayList<String>();
            filteredPaths.forEach(path -> pathStrings.add(path.toString()));
            config.setValue(DefaultSettings.CODE_EXTRACTOR_FILES, pathStrings);
            // If no paths are included after filtering, the extraction
            // does not need to run
        }

        config.setValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL,
            extractCm);

        // Filter variability model files
        filteredPaths = filterInput(
            config.getValue(
                IncrementalAnalysisSettings.VARIABILITY_MODEL_FILTER_CLASS),
            inputSourceDir, diffFileForFiltering,
            config.getValue(DefaultSettings.VARIABILITY_EXTRACTOR_FILE_REGEX),
            true);
        boolean extractVm = !filteredPaths.isEmpty();
        config.setValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL,
            extractVm);

        // Filter build model files
        if (extractVm) {
            // if vm was updated, always extract bm aswell as it depends
            // on the vm
            config.setValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL,
                true);
        } else {
            filteredPaths = filterInput(
                config.getValue(
                    IncrementalAnalysisSettings.BUILD_MODEL_FILTER_CLASS),
                inputSourceDir, diffFileForFiltering,
                config.getValue(DefaultSettings.BUILD_EXTRACTOR_FILE_REGEX),
                true);
            boolean extractBm = !filteredPaths.isEmpty();
            config.setValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL,
                extractBm);
        }
    }

    /**
     * Filters input using the class defined by filterClassName. This should be
     * a class available in the classpath and implementing InputFilter.
     *
     * @param filterClassName
     *            the filter class name
     * @param inputSourceDir
     *            the input source dir
     * @param inputDiff
     *            the input diff file
     * @param regex
     *            the regular expression describing which files to include
     * @param includeDeletions
     *            defines whether deletions are included
     * @return the collection of resulting paths
     * @throws SetUpException
     *             the set up exception
     */
    protected Collection<Path> filterInput(String filterClassName,
        File inputSourceDir, DiffFile inputDiff, Pattern regex,
        boolean includeDeletions) throws SetUpException {
        Collection<Path> paths = null;
        // Call the method getFilteredResult for filterClassName via
        // reflection-api
        try {
            Class<InputFilter> filterClass =
                (Class<InputFilter>) Class.forName(filterClassName);
            Object filterObject = filterClass.getConstructor(File.class,
                DiffFile.class, Pattern.class, boolean.class).newInstance(
                    inputSourceDir, inputDiff, regex, includeDeletions);
            if (filterObject instanceof InputFilter) {
                Method getFilteredResultMethod =
                    filterClass.getMethod("getFilteredResult");
                paths = (Collection<Path>) getFilteredResultMethod
                    .invoke(filterObject);
            } else {
                throw new SetUpException(
                    "The class name provided for the filter does not appear to extend the InputFilter class");
            }

        } catch (ClassNotFoundException | IllegalAccessException
            | InstantiationException | NoSuchMethodException
            | InvocationTargetException e) {
            LOGGER.logException("The specified filter class could not be used",
                e);
            throw new SetUpException(
                "The specified filter could not be used: " + e.getMessage());
        }
        return paths;

    }

    /**
     * Generates a {@link DiffFile} using the provided {@link DiffAnalyzer}
     * class.
     *
     * @param analyzerClassName
     *            the analyzer class name
     * @param inputGitDiff
     *            the input git diff
     * @return the collection
     * @throws SetUpException
     *             the set up exception
     */
    @SuppressWarnings("unchecked")
    protected DiffFile generateDiffFile(String analyzerClassName,
        File inputGitDiff) throws SetUpException {
        DiffFile diffFile = null;
        // Call the method getFilteredResult for filterClassName via
        // reflection-api
        try {
            Class<DiffAnalyzer> analyzerClass =
                (Class<DiffAnalyzer>) Class.forName(analyzerClassName);
            Object analyzerObject =
                analyzerClass.getConstructor().newInstance();
            Method getFilteredResultMethod =
                analyzerClass.getMethod("generateDiffFile", File.class);
            LOGGER.logInfo(
                "Analyzing git-diff with " + analyzerClass.getSimpleName()
                    + ". This may take a while for large git-diffs.");
            diffFile = (DiffFile) getFilteredResultMethod.invoke(analyzerObject,
                inputGitDiff);

        } catch (ClassNotFoundException | IllegalAccessException
            | InstantiationException | NoSuchMethodException
            | InvocationTargetException e) {
            throw new SetUpException("The specified DiffAnalyzer class \""
                + analyzerClassName + "\" could not be used: "
                + e.getClass().getName() + "\n" + e.getMessage());
        }
        return diffFile;

    }

}
