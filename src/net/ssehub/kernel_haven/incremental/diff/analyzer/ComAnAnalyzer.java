package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

/**
 * A {@link VariabilityChangeAnalyzer}-Implementation that analyzes for
 * variability-changes within a commit represented by a {@link DiffFile}.
 * 
 * @author Christian Kroeher, moritz
 *
 */
public class ComAnAnalyzer implements VariabilityChangeAnalyzer {

    private static String comAnKConfigPattern = ".*/Kconfig((\\.|\\-|\\_|\\+|\\~).*)?";
    private static String comAnBuildPattern = ".*/(Makefile|Kbuild)((\\.|\\-|\\_|\\+|\\~).*)?";
    private static String comAnCodePattern = ".*/.*\\.[hcS]((\\.|\\-|\\_|\\+|\\~).*)?";

    /**
     * Generate diff file.
     *
     * @param file the file
     * @return the diff file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse ()
     */
    @Override
    public void analyzeDiffFile(DiffFile diffFile, Configuration config) throws IOException {
        Properties pluginProperties = new Properties();
        pluginProperties.setProperty("core.version_control_system", "git");
        pluginProperties.setProperty("analysis.output", ""); // Unused but mandatory
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.vm_files_regex", comAnKConfigPattern);
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.code_files_regex", comAnCodePattern);
        pluginProperties.setProperty("analysis.dead_code_change_analyzer.build_files_regex", comAnBuildPattern);

        CommitQueue commitQueue = new CommitQueue(1);
        // Instantiate the commit extractor and analyzer

        Map<String, AnalysisResult> analysisResults = null;

        try {
            GitCommitExtractor commitExtractor = new GitCommitExtractor(pluginProperties, commitQueue);
            DeadCodeChangeAnalyzer commitAnalyzer = new DeadCodeChangeAnalyzer(pluginProperties, commitQueue);
            // Extract the commits based on the commit files in the test commits directory
            commitQueue.setState(QueueState.OPEN);
            StringJoiner joiner = new StringJoiner("\n");
            File originalDiffFile = config.getValue(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE);
            joiner.add("commit " + originalDiffFile.getName());
            Files.readAllLines(originalDiffFile.toPath()).forEach(line -> joiner.add(line));

            commitExtractor.extract(joiner.toString());
            commitQueue.setState(QueueState.CLOSED); // Actual closing after all commits are analyzed
            // Analyze the extracted commits
            if (commitAnalyzer.analyze()) {
                analysisResults = commitAnalyzer.getResults();
            }

        } catch (ExtractionSetupException | AnalysisSetupException e) {
            e.printStackTrace();
        }

        if (analysisResults != null && analysisResults.keySet().size() == 1) {
            AnalysisResult result = analysisResults.get(analysisResults.keySet().iterator().next());
            boolean buildChanges = result.getRelevantBuildChanges();
            boolean vmChanges = result.getRelevantVariabilityModelChanges();
            Set<Path> changedCodeFiles = new HashSet<Path>();
            result.getRelevantCodeChanges().forEach(entry -> changedCodeFiles.add(Paths.get(entry.substring(1))));

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

}
