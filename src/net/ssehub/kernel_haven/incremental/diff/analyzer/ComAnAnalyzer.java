/*
 * 
 */
package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import net.ssehub.comani.analysis.AnalysisSetupException;
import net.ssehub.comani.analysis.deadcodechange.core.DeadCodeChangeAnalyzer;
import net.ssehub.comani.analysis.deadcodechange.diff.AnalysisResult;
import net.ssehub.comani.data.CommitQueue;
import net.ssehub.comani.data.CommitQueue.QueueState;
import net.ssehub.comani.extraction.ExtractionSetupException;
import net.ssehub.comani.extraction.git.GitCommitExtractor;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFile;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.VariabilityChange;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.util.Logger;

/**
 * A {@link VariabilityChangeAnalyzer}-Implementation that analyzes for
 * variability-changes within a commit represented by a {@link DiffFile}.
 * 
 * @author Christian Kroeher, Moritz
 *
 */
public class ComAnAnalyzer implements VariabilityChangeAnalyzer {

    /** The com an K config pattern. */
    private static String comAnKConfigPattern = ".*/Kconfig((\\.|\\-|\\_|\\+|\\~).*)?";

    /** The com an build pattern. */
    private static String comAnBuildPattern = ".*/(Makefile|Kbuild)((\\.|\\-|\\_|\\+|\\~).*)?";

    /** The com an code pattern. */
    private static String comAnCodePattern = ".*/.*\\.[hcS]((\\.|\\-|\\_|\\+|\\~).*)?";

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse ()
     */
    @Override
    public void analyzeDiffFile(DiffFile diffFile, Configuration config) throws IOException {
        AnalysisResult comAnResult = null;
        try {
            comAnResult = getComAnResults(config);
        } catch (IOException exc) {
            Logger.get().logException("Failed to extract variability change information from diff file with ComAnIn",
                    exc);
            throw exc;
        }
        if (comAnResult != null) {
            boolean buildChanges = comAnResult.getRelevantBuildChanges();
            boolean vmChanges = comAnResult.getRelevantVariabilityModelChanges();
            Set<Path> changedCodeFiles = new HashSet<Path>();
            comAnResult.getRelevantCodeChanges().forEach(entry -> changedCodeFiles.add(Paths.get(entry.substring(1))));

            Pattern buildPattern = Pattern.compile(comAnBuildPattern);
            Pattern vmPattern = Pattern.compile(comAnKConfigPattern);
            Pattern codePattern = Pattern.compile(comAnCodePattern);

            for (FileEntry entry : diffFile.getEntries()) {
                if (changedCodeFiles.contains(Paths.get(entry.getPath().toString()))) {
                    entry.setVariabilityChange(VariabilityChange.CHANGE);
                } else if (buildPattern.matcher(entry.getPath().toString()).find()) {
                    if (buildChanges) {
                        entry.setVariabilityChange(VariabilityChange.CHANGE);
                    } else {
                        entry.setVariabilityChange(VariabilityChange.NO_CHANGE);
                    }
                } else if (vmPattern.matcher(entry.getPath().toString()).find()) {
                    if (vmChanges) {
                        entry.setVariabilityChange(VariabilityChange.CHANGE);
                    } else {
                        entry.setVariabilityChange(VariabilityChange.NO_CHANGE);
                    }
                } else if (codePattern.matcher(entry.getPath().toString()).find()) {
                    entry.setVariabilityChange(VariabilityChange.NO_CHANGE);
                } else {
                    entry.setVariabilityChange(VariabilityChange.NOT_A_VARIABILITY_FILE);
                }
            }
        }
    }

    /**
     * Gets the results from ComAn describing differences in variability.
     *
     * @param config the config
     * @return the com an results
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public AnalysisResult getComAnResults(Configuration config) throws IOException {
        Properties pluginProperties = new Properties();

        pluginProperties.setProperty("core.version_control_system", "git");
        pluginProperties.setProperty("analysis.output", ""); // Unused but mandatory
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.vm_files_regex", comAnKConfigPattern);
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.code_files_regex", comAnCodePattern);
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.build_files_regex", comAnBuildPattern);

        AnalysisResult result = null;

        try {
            Map<String, AnalysisResult> analysisResults = null;
            CommitQueue commitQueue = new CommitQueue(1);
            GitCommitExtractor commitExtractor = new GitCommitExtractor(pluginProperties, commitQueue);
            DeadCodeChangeAnalyzer commitAnalyzer = new DeadCodeChangeAnalyzer(pluginProperties, commitQueue);
            // Extract the commits based on the commit files in the test commits directory
            commitQueue.setState(QueueState.OPEN);

            StringJoiner joiner = new StringJoiner("\n");
            File originalDiffFile = config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
            try (BufferedReader br = new BufferedReader(new FileReader(originalDiffFile))) {
                String currentLine = br.readLine();
                if (!currentLine.startsWith("commit ")) {
                    joiner.add("commit " + originalDiffFile.getName());
                }
                while (currentLine != null) {
                    joiner.add(currentLine);
                    currentLine = br.readLine();
                }

            }

            commitExtractor.extract(joiner.toString());
            commitQueue.setState(QueueState.CLOSED); // Actual closing after all commits are analyzed
            // Analyze the extracted commits
            if (commitAnalyzer.analyze()) {
                analysisResults = commitAnalyzer.getResults();
            }
            if (analysisResults != null && analysisResults.keySet().size() == 1) {
                result = analysisResults.get(analysisResults.keySet().iterator().next());
            }

        } catch (ExtractionSetupException | AnalysisSetupException e) {
            e.printStackTrace();
        }

        return result;
    }

}
