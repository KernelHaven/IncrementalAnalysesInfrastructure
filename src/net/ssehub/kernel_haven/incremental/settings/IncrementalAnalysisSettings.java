package net.ssehub.kernel_haven.incremental.settings;

import static net.ssehub.kernel_haven.config.Setting.Type.BOOLEAN;
import static net.ssehub.kernel_haven.config.Setting.Type.DIRECTORY;
import static net.ssehub.kernel_haven.config.Setting.Type.FILE;
import static net.ssehub.kernel_haven.config.Setting.Type.STRING;
import static net.ssehub.kernel_haven.config.Setting.Type.STRING_LIST;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.util.Logger;

/**
 * This class represents the settings for incremental analyses.
 * 
 * @author moritz
 */
public class IncrementalAnalysisSettings {

    // CHECKSTYLE:OFF
    public static final Setting<File> SOURCE_TREE_DIFF_FILE =
            new Setting<>("incremental.input.source_tree_diff", FILE, true, "git.diff",
                    "Diff-file describing the changes from the previously analyzed increment to the next one.");

    public static final Setting<String> CODE_MODEL_FILTER_CLASS = new Setting<>("incremental.code.filter", STRING, true,
            "net.ssehub.kernel_haven.incremental.preparation.filter.DefaultFilter",
            "name of the class used to filter the input for the code-model in the incremental analysis");

    public static final Setting<String> VARIABILITY_CHANGE_ANALYZER_CLASS =
            new Setting<>("incremental.variability_change_analyzer.class", STRING, true,
                    "net.ssehub.kernel_haven.incremental.diff.analyzer.ComAnAnalyzer",
                    "name of the class used to analyze the git-diff file for variability changes");

    public static final Setting<Boolean> EXECUTE_VARIABILITY_CHANGE_ANALYZER =
            new Setting<>("incremental.variability_change_analyzer.execute", BOOLEAN, true, "FALSE",
                    "defines whether a Variability Analyzer is used");

    public static final Setting<String> VARIABILITY_MODEL_FILTER_CLASS = new Setting<>("incremental.variability.filter",
            STRING, true, "net.ssehub.kernel_haven.incremental.preparation.filter.DefaultFilter",
            "name of the class used to filter the input for the variability-model in the incremental analysis");

    public static final Setting<String> BUILD_MODEL_FILTER_CLASS = new Setting<>("incremental.build.filter", STRING,
            true, "net.ssehub.kernel_haven.incremental.preparation.filter.DefaultFilter",
            "name of the class used to filter the input for the build-model in the incremental analysis");

    public static final Setting<File> HYBRID_CACHE_DIRECTORY = new Setting<>("incremental.hybrid_cache.dir", DIRECTORY,
            true, "hybrid-cache/", "Directory which represents the cache for incremental analyses");

    public static final Setting<Boolean> EXTRACT_CODE_MODEL = new Setting<>("incremental.code.extract_cm", BOOLEAN,
            true, "FALSE", "This setting automatically gets set by IncrementalPreparation");

    public static final Setting<Boolean> EXTRACT_VARIABILITY_MODEL = new Setting<>("incremental.variability.extract_vm",
            BOOLEAN, true, "FALSE", "This setting automatically gets set by IncrementalPreparation");

    public static final Setting<Boolean> EXTRACT_BUILD_MODEL = new Setting<>("incremental.build.extract_bm", BOOLEAN,
            true, "FALSE", "This setting automatically gets set by IncrementalPreparation");

    public static final Setting<List<String>> DELETED_FILES = new Setting<>("incremental.postextraction.deleted_files",
            STRING_LIST, false, null, "This setting automatically gets set by IncrementalPreparation");

    public static final Setting<Boolean> UPDATE_CODE_LINES = new Setting<>("incremental.lines.update_lines", BOOLEAN,
            true, "FALSE",
            "Defines whether the linenumber information should be updated for code files that were not modified through extraction.");

    public static final Setting<Boolean> ROLLBACK = new Setting<>("incremental.rollback", BOOLEAN, true, "FALSE",
            "This setting defines whether a rollback sould be performed");

    // CHECKSTYLE:ON
    /**
     * Holds all declared setting constants.
     */
    private static final Set<Setting<?>> SETTINGS = new HashSet<>();

    /**
     * Instantiates a new incremental analysis settings.
     */
    private IncrementalAnalysisSettings() {

    }

    static {
        for (Field field : IncrementalAnalysisSettings.class.getFields()) {
            if (Setting.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())) {
                try {
                    SETTINGS.add((Setting<?>) field.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    Logger.get().logException("Could not add Setting.", e);
                }
            }
        }
    }

    /**
     * Registers all settings declared in this class to the given configuration
     * object.
     * 
     * @param config The configuration to register the settings to.
     * 
     * @throws SetUpException If any setting restrictions are violated.
     */
    public static void registerAllSettings(Configuration config) throws SetUpException {
        for (Setting<?> setting : SETTINGS) {
            config.registerSetting(setting);
        }
    }
}
