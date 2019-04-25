package net.ssehub.kernel_haven.incremental.preparation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.diff.analyzer.VariabilityChangeAnalyzer;
import net.ssehub.kernel_haven.incremental.diff.applier.DiffApplier;
import net.ssehub.kernel_haven.incremental.diff.applier.FileReplacingDiffApplier;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.preparation.filter.AdditionFilter;
import net.ssehub.kernel_haven.incremental.preparation.filter.InputFilter;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.storage.HybridCache;
import net.ssehub.kernel_haven.incremental.util.FileUtil;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Preparation task for incremental analyses. This class is used to integrate a
 * diff on the filebase of the source tree and subsequently select a subset of
 * the resulting files for extraction and analyses.
 * {@link IncrementalPreparation} must be used as preparation when working with
 * an incremental analysis.
 * 
 * @author Moritz
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
        long start = System.nanoTime();

        IncrementalAnalysisSettings.registerAllSettings(config);

        File inputDiff = config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
        File inputSourceDir = config.getValue(DefaultSettings.SOURCE_TREE);

        LOGGER.logInfo("Parsing diff file - this may take a few minutes ...");
        DiffFile diffFile = readDiffFile(inputDiff);

        // First check if the diff file was read successfully
        if (diffFile == null) {
            LOGGER.logError("Diff file " + inputDiff.getPath()
                    + " could not be read! Perhaps file is not a valid git-diff file.");
            throw new SetUpException("Diff file could not be read! Perhaps file is not a valid git-diff file.");
        }

        // If this is a rollback execution, only a rollback and nothing more
        // will be done
        if (config.getValue(IncrementalAnalysisSettings.ROLLBACK)) {
            // Execution will stop after rollback is complete
            handleRollback(config, inputSourceDir, diffFile);

        } else {
            // merge the changes described by the diff file to the codebase
            // and define targets for extraction
            handleMergeAndPrepareExtraction(config, inputSourceDir, diffFile);
        }

        long totalTime = System.nanoTime() - start;
        // Finish and let KernelHaven run
        LOGGER.logDebug(this.getClass().getSimpleName() + " duration:"
                + TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS) + "ms");
    }

    /**
     * Handle rollback.
     *
     * @param config         the config
     * @param inputSourceDir the input source dir
     * @param diffFile       the diff file
     */
    private void handleRollback(Configuration config, File inputSourceDir, DiffFile diffFile) {
        DiffApplier diffApplier = new FileReplacingDiffApplier(inputSourceDir, diffFile);
        // Handle rollback
        boolean revertSuccessful = diffApplier.revertChanges();
        HybridCache hybridCache =
                new HybridCache((File) config.getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY));
        try {
            hybridCache.rollback();
        } catch (IOException e) {
            revertSuccessful = false;
            LOGGER.logException("Could not revert changes in " + HybridCache.class.getSimpleName(), e);
        }

        // Stop execution after rollback
        if (revertSuccessful) {
            LOGGER.logInfo("Rollback successful.");
        }
    }

    /**
     * Handle merge and prepare extraction.
     *
     * @param config         the config
     * @param inputSourceDir the input source dir
     * @param diffFile       the diff file
     * @throws SetUpException the set up exception
     */
    private void handleMergeAndPrepareExtraction(Configuration config, File inputSourceDir, DiffFile diffFile)
            throws SetUpException {
        DiffApplier diffApplier = new FileReplacingDiffApplier(inputSourceDir, diffFile);
        // Merge changes
        boolean mergeSuccessful = diffApplier.mergeChanges();
        // only continue if merge was successful
        if (!mergeSuccessful) {
            LOGGER.logError("Could not merge provided diff with existing input files!\n"
                    + "The diff-file must describe changes that can"
                    + " be applied to the set of input-files that are to be analyzed. \n"
                    + "Stopping execution of KernelHaven.");
            throw new SetUpException("Could not merge provided diff with existing input files!");
        } else {
            // Only analyze for variability changes if required by the configuration
            if (config.getValue(IncrementalAnalysisSettings.EXECUTE_VARIABILITY_CHANGE_ANALYZER)) {
                analyzeVariabilityChanges(
                        config.getValue(IncrementalAnalysisSettings.VARIABILITY_CHANGE_ANALYZER_CLASS), diffFile,
                        config);
            }

            // Define targets for extraction.
            defineTargetsForExtraction(config, inputSourceDir, diffFile);

            /*
             * Tell IncrementalPostExtraction which files got deleted. This is required so
             * that the according models can be deleted from {@link
             * net.ssehub.kernel_haven.incremental.storage.HybridCache}.
             */
            List<String> deletedFiles = new ArrayList<>();
            for (FileEntry entry : diffFile.getEntries()) {
                if (FileEntry.FileChange.DELETION.equals(entry.getType())) {
                    deletedFiles.add(entry.getPath().toString());
                }
            }
            config.setValue(IncrementalAnalysisSettings.DELETED_FILES, deletedFiles);

            // Overwrite setting to preemptively start extractors as
            // extractors are only started when models need to be extracted.
            config.setValue(DefaultSettings.ANALYSIS_PIPELINE_START_EXTRACTORS, false);

        }
    }

    /**
     * Read diff file.
     *
     * @param inputDiff the input diff
     * @return the diff file
     * @throws SetUpException the set up exception
     */
    private DiffFile readDiffFile(File inputDiff) throws SetUpException {
        // Check if git diff file is empty. If an exception is thrown while
        // accessing the file, we also handle that here.
        boolean emptyFile = false;
        try {
            emptyFile = FileUtil.isEmptyFile(inputDiff);
        } catch (IOException exc) {
            LOGGER.logException("Could not access file " + inputDiff.getPath() + "!", exc);
            throw new SetUpException("Diff file could not be read! Make sure you have read access to the file.");
        }

        // Try to initialize a diff-applier with the parsed version of the diff
        // file.
        DiffFile diffFile = null;
        if (!emptyFile) {
            diffFile = DiffFileParser.parse(inputDiff);

        } else {
            LOGGER.logError("Diff file " + inputDiff.getPath() + " is empty! No new changes...");
            throw new SetUpException("Stopping execution as diff file was empty.");
        }
        return diffFile;
    }

    /**
     * Define targets for extraction. This modifies the configuration to include
     * only the relevant files for extraction through the extractors of KernelHaven
     *
     * @param config               the config
     * @param inputSourceDir       the input source dir
     * @param diffFileForFiltering the diff file for filtering
     * @throws SetUpException the set up exception
     */
    private void defineTargetsForExtraction(Configuration config, File inputSourceDir, DiffFile diffFileForFiltering)
            throws SetUpException {
        // Filter code model files
        Collection<Path> filteredPaths =
                filterInput(config.getValue(IncrementalAnalysisSettings.CODE_MODEL_FILTER_CLASS), inputSourceDir,
                        diffFileForFiltering, config.getValue(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX), false);

        boolean extractCm = false;
        if (!filteredPaths.isEmpty()) {
            extractCm = true;
            ArrayList<String> pathStrings = new ArrayList<>();
            filteredPaths.forEach(path -> pathStrings.add(path.toString()));
            config.setValue(DefaultSettings.CODE_EXTRACTOR_FILES, pathStrings);
            // If no paths are included after filtering, the extraction
            // does not need to run
        }

        config.setValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL, extractCm);

        Collection<Path> addedCodeFiles = filterInput(AdditionFilter.class.getName(), inputSourceDir,
                diffFileForFiltering, config.getValue(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX), false);

        // Filter variability model files
        filteredPaths =
                filterInput(config.getValue(IncrementalAnalysisSettings.VARIABILITY_MODEL_FILTER_CLASS), inputSourceDir,
                        diffFileForFiltering, config.getValue(DefaultSettings.VARIABILITY_EXTRACTOR_FILE_REGEX), true);
        boolean extractVm = !filteredPaths.isEmpty();

        config.setValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL, extractVm);
        config.setValue(IncrementalAnalysisSettings.AUXILLARY_BUILD_MODEL_EXTRACTION, false);

        // Filter build model files
        if (extractVm) {
            // if vm was updated, always extract bm aswell as it depends
            // on the vm
            config.setValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL, true);
        } else {
            filteredPaths =
                    filterInput(config.getValue(IncrementalAnalysisSettings.BUILD_MODEL_FILTER_CLASS), inputSourceDir,
                            diffFileForFiltering, config.getValue(DefaultSettings.BUILD_EXTRACTOR_FILE_REGEX), true);
            boolean extractBm = !filteredPaths.isEmpty();
            if (!extractBm && !addedCodeFiles.isEmpty()) {
                extractBm = true;
                LOGGER.logInfo(
                        "Build model will be reextracted due to one or more added code files that needs to be considered when looking at the build process. "
                                + "The build model itself did not change. "
                                + "Therefore this extraction is considered an auxillary change to the build model.");
                config.setValue(IncrementalAnalysisSettings.AUXILLARY_BUILD_MODEL_EXTRACTION, true);
            }
            config.setValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL, extractBm);
        }
    }

    /**
     * Filters input using the class defined by filterClassName. This should be a
     * class available in the classpath and implementing InputFilter.
     *
     * @param filterClassName  the filter class name
     * @param inputSourceDir   the input source dir
     * @param inputDiff        the input diff file
     * @param regex            the regular expression describing which files to
     *                         include
     * @param includeDeletions defines whether deletions are included
     * @return the collection of resulting paths
     * @throws SetUpException the set up exception
     */
    protected Collection<Path> filterInput(String filterClassName, File inputSourceDir, DiffFile inputDiff,
            Pattern regex, boolean includeDeletions) throws SetUpException {
        Collection<Path> paths = null;
        // Call the method getFilteredResult for filterClassName via
        // reflection-api
        try {
            Object filterObject = Class.forName(filterClassName)
                    .getConstructor(File.class, DiffFile.class, Pattern.class, boolean.class)
                    .newInstance(inputSourceDir, inputDiff, regex, includeDeletions);
            InputFilter filter = InputFilter.class.cast(filterObject);
            paths = filter.getFilteredResult();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e) {
            LOGGER.logException("The specified filter class could not be used", e);
            throw new SetUpException("The specified filter could not be used: " + e.getMessage());
        }
        return paths;

    }

    /**
     * Writes variability-information to a {@link DiffFile} using the provided
     * {@link VariabilityChangeAnalyzer} class.
     *
     * @param analyzerClassName the analyzer class name
     * @param diffFile          the diff file
     * @param config            the config
     * @throws SetUpException the set up exception
     */
    @SuppressWarnings("unchecked")
    protected void analyzeVariabilityChanges(String analyzerClassName, DiffFile diffFile, Configuration config)
            throws SetUpException {
        // Call the method getFilteredResult for filterClassName via
        // reflection-api
        try {
            LOGGER.logInfo(
                    "Analyzing git-diff with " + analyzerClassName + ". This may take a while for large git-diffs.");
            Class<VariabilityChangeAnalyzer> analyzerClass =
                    (Class<VariabilityChangeAnalyzer>) Class.forName(analyzerClassName);
            VariabilityChangeAnalyzer analyzer =
                    VariabilityChangeAnalyzer.class.cast(analyzerClass.getConstructor().newInstance());
            analyzer.analyzeDiffFile(diffFile, config);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException | IOException e) {
            e.printStackTrace();
            throw new SetUpException("The specified DiffAnalyzer class \"" + analyzerClassName
                    + "\" could not be used successfully: " + e.getClass().getName() + "\n" + e.getMessage());
        }

    }

}
