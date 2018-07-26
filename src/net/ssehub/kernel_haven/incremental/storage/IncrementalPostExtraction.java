package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.AnalysisComponent;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.diff.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.FileEntry;
import net.ssehub.kernel_haven.incremental.diff.analyzer.SimpleDiffAnalyzer;
import net.ssehub.kernel_haven.incremental.diff.linecount.LineCounter;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
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
    private AnalysisComponent<SourceFile> cmComponent;

    /** The config. */
    private Configuration config;

    /** The bm component. */
    private AnalysisComponent<BuildModel> bmComponent;

    /** The vm component. */
    private AnalysisComponent<VariabilityModel> vmComponent;

    /**
     * Instantiates a new IncremenmtalPostExtraction.
     *
     * @param config
     *            the config
     * @param cmComponent
     *            the cm component
     * @param bmComponent
     *            the bm component
     * @param vmComponent
     *            the vm component
     * @throws SetUpException
     *             thrown if required parameters were not configured correctly.
     */
    public IncrementalPostExtraction(Configuration config,
        AnalysisComponent<SourceFile> cmComponent,
        AnalysisComponent<BuildModel> bmComponent,
        AnalysisComponent<VariabilityModel> vmComponent) throws SetUpException {
        super(config);
        this.config = config;
        IncrementalAnalysisSettings.registerAllSettings(config);
        this.cmComponent = cmComponent;
        this.bmComponent = bmComponent;
        this.vmComponent = vmComponent;
    }

    /**
     * Try join thread.
     *
     * @param thread
     *            the thread
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
        DiffFile diffFile = getDiffFile();
        HybridCache hybridCache = new HybridCache(config
            .getValue(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY));
        hybridCache.clearChangeHistory();

        // start threads for each model-type so they can run parallel
        Thread cmThread = null;
        if (config.getValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL)) {
            cmThread = new Thread() {
                public void run() {
                    codeModelExtraction(hybridCache, diffFile);
                }
            };
            cmThread.start();
        }

        Thread vmThread = null;
        if (config
            .getValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL)) {
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
        try {
            updateCodeLineInformation(diffFile, hybridCache);
        } catch (IllegalArgumentException | IOException | FormatException exc) {
            LOGGER.logException("Could not update codelines for models", exc);
        }

        this.addResult(hybridCache);
    }

    /**
     * Update code line information.
     *
     * @param diffFile
     *            the diff file
     * @param hybridCache
     *            the hybrid cache
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    private void updateCodeLineInformation(DiffFile diffFile,
        HybridCache hybridCache)
        throws IllegalArgumentException, IOException, FormatException {

        Collection<String> codeExtractorFiles =
            config.getValue(DefaultSettings.CODE_EXTRACTOR_FILES);
        Collection<Path> extractedPaths = new ArrayList<Path>();
        codeExtractorFiles.forEach(file -> extractedPaths.add(Paths.get(file)));

        LineCounter counter = new LineCounter(
            config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE),
            extractedPaths,
            config.getValue(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX));

        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileEntry.Type.MODIFICATION)
                && !extractedPaths.contains(entry.getPath())) {
                SourceFile srcFile =
                    hybridCache.readCm(entry.getPath().toFile());
                if (srcFile != null) {
                    Logger.get().logDebug(
                        "Updating lines for file: " + entry.getPath());
                    // Iterate over sourcefile and update line numbers
                    Iterator<CodeElement> itr = srcFile.iterator();

                    while (itr.hasNext()) {
                        CodeElement element = itr.next();
                        // recurively handle element and nested elements
                        updateLineNumbersForElement(counter, element);
                    }

                    hybridCache.write(srcFile);
                }
            }
        }

    }

    /**
     * Update line numbers for element.
     *
     * @param counter
     *            the counter
     * @param element
     *            the element
     */
    private void updateLineNumbersForElement(LineCounter counter,
        CodeElement element) {

        for (CodeElement nested : element.iterateNestedElements()) {
            updateLineNumbersForElement(counter, nested);
        }
        Path sourceFilePath = element.getSourceFile().toPath();

        int previousStart = element.getLineStart();
        int previousEnd = element.getLineEnd();

        LOGGER.logDebug(
            "Lines before: start=" + previousStart + ", end=" + previousEnd);
        if (previousStart >= 0) {
            int newStart =
                counter.getNewLineNumber(sourceFilePath, previousStart);
            element.setLineStart(newStart);
            LOGGER.logDebug("Setting new start: start=" + newStart);
        }
        if (previousEnd >= 0) {
            int newEnd = counter.getNewLineNumber(sourceFilePath, previousEnd);
            element.setLineEnd(newEnd);
            LOGGER.logDebug("Setting new end: end=" + newEnd);
        }

    }

    /**
     * Variability model extraction.
     *
     * @param hybridCache
     *            the hybrid cache to write the extracated results to.
     */
    private void variabilityModelExtraction(HybridCache hybridCache) {
        VariabilityModel variabilityModel;
        // CHECKSTYLE:OFF
        if ((variabilityModel = vmComponent.getNextResult()) != null) {
            // CHECKSTYLE:ON
            try {
                hybridCache.write(variabilityModel);
            } catch (IOException e) {
                LOGGER.logException(
                    "Could not write variability-model to HybridCache", e);
            }
        } else {
            try {
                hybridCache.deleteVariabilityModel();
            } catch (IOException e) {
                LOGGER.logException(
                    "Could not delete variability-model from HybridCache", e);
            }
        }
    }

    /**
     * Build model extraction.
     *
     * @param hybridCache
     *            the hybrid cache to write the extracated results to.
     */
    private void buildModelExtraction(HybridCache hybridCache) {
        BuildModel buildModel;
        // CHECKSTYLE:OFF
        if ((buildModel = bmComponent.getNextResult()) != null) {
            // CHECKSTYLE:ON
            try {
                hybridCache.write(buildModel);
            } catch (IOException e) {
                LOGGER.logException(
                    "Could not write build-model to HybridCache", e);
            }
        } else {
            try {
                hybridCache.deleteBuildModel();
            } catch (IOException e) {
                LOGGER.logException(
                    "Could not delete build-model from HybridCache", e);
            }
        }
    }

    /**
     * Gets the diff file.
     *
     * @return the diff file
     */
    public DiffFile getDiffFile() {
        DiffFile diffFile = null;
        File originalDiffFile =
            config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
        File parsedDiffFile =
            new File(originalDiffFile.getAbsolutePath() + config
                .getValue(IncrementalAnalysisSettings.PARSED_DIFF_FILE_SUFFIX));

        // Deletion of models for files removed in the diff-file
        if (parsedDiffFile.exists()) {
            LOGGER.logInfo("Reusing parsed diff-file: "
                + parsedDiffFile.getAbsolutePath());
            try {
                diffFile = DiffFile.load(parsedDiffFile);
            } catch (IOException | ParseException e) {
                LOGGER.logException("Could not reuse parsed diff-file: "
                    + parsedDiffFile.getAbsolutePath(), e);
            }
        }
        if (diffFile == null) {
            // Try to reuse existing parsed diff if available
            // otherwise generate new
            if (diffFile == null) {
                LOGGER.logInfo("Parsing original diff-file: "
                    + originalDiffFile.getAbsolutePath());
                try {
                    diffFile = new SimpleDiffAnalyzer()
                        .generateDiffFile(originalDiffFile);
                } catch (IOException e) {
                    String error = "This is a major problem as it might"
                        + " result in an inconsistent state of your"
                        + " HybridCache-directory.";
                    LOGGER.logError("Could not parse diff-file: "
                        + originalDiffFile.getAbsolutePath(), error);
                }
            }
        }
        return diffFile;
    }

    /**
     * Code model extraction.
     *
     * @param hybridCache
     *            the hybrid cache to write the extracted results to.
     * @param diffFile
     *            the diff file
     */
    private void codeModelExtraction(HybridCache hybridCache,
        DiffFile diffFile) {
        SourceFile file;

        // delete all models corresponding to deleted files
        for (FileEntry entry : diffFile.getEntries()) {
            if (entry.getType().equals(FileEntry.Type.DELETION)) {
                try {
                    LOGGER.logDebug(
                        "Deleting model because of DiffEntry: " + entry);
                    hybridCache.deleteCodeModel(entry.getPath().toFile());
                } catch (IOException exception) {
                    LOGGER.logException("Could not delete CodeModel-File. "
                        + "This may result in an inconsistent state of Hybridcache. "
                        + "To fix an inconsistent state you can either do a rollback "
                        + "or extract all models from scratch.", exception);
                }
            }
        }

        // Add new models to hybridCache
        while ((file = cmComponent.getNextResult()) != null) {
            try {
                hybridCache.write(file);
            } catch (IOException e) {
                LOGGER.logException("Could not write sourcefile to HybridCache",
                    e);
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