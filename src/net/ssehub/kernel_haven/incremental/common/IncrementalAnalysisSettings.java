package net.ssehub.kernel_haven.incremental.common;

import static net.ssehub.kernel_haven.config.Setting.Type.FILE;
import static net.ssehub.kernel_haven.config.Setting.Type.STRING;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.Setting;

// TODO: Auto-generated Javadoc
/**
 * The Class IncrementalAnalysisSettings.
 */
public class IncrementalAnalysisSettings {

	public static final Setting<File> SOURCE_TREE_DIFF_FILE = new Setting<File>("incemental.input.source_tree_diff",
			FILE, true, "git.diff",
			"Diff-file describing the changes from the previously analyzed increment to the next one.");
	public static final Setting<String> FILTER_CLASS = new Setting<String>("incemental.filter", STRING, true,
			"ModelStoragePipeline.src.net.ssehub.kernel_haven.incremental.preparation.BogusFilter",
			"name of the class used to filter the input for the incremental analysis");

	/**
	 * Instantiates a new incremental analysis settings.
	 */
	private IncrementalAnalysisSettings() {

	}

	/**
	 * Holds all declared setting constants.
	 */
	private static final Set<Setting<?>> SETTINGS = new HashSet<>();

	static {
		for (Field field : IncrementalAnalysisSettings.class.getFields()) {
			if (Setting.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())) {
				try {
					SETTINGS.add((Setting<?>) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Registers all settings declared in this class to the given configuration
	 * object.
	 * 
	 * @param config
	 *            The configuration to register the settings to.
	 * 
	 * @throws SetUpException
	 *             If any setting restrictions are violated.
	 */
	public static void registerAllSettings(Configuration config) throws SetUpException {
		for (Setting<?> setting : SETTINGS) {
			config.registerSetting(setting);
		}
	}
}
