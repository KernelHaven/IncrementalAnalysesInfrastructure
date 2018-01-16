package net.ssehub.kernel_haven.incremental.common;

import static net.ssehub.kernel_haven.config.Setting.Type.DIRECTORY;
import static net.ssehub.kernel_haven.config.Setting.Type.STRING;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.EnumSetting;
import net.ssehub.kernel_haven.config.Setting;

// TODO: Auto-generated Javadoc
/**
 * The Class IncrementalAnalysisSettings.
 */
public class IncrementalAnalysisSettings {

	/** The Constant SOURCE_DIR. */
	public static final Setting<File> SOURCE_DIR = new Setting<>("incremental.source_dir", DIRECTORY, false,
			"./incremental/source", "TODO");

	/** The Constant MODEL_DIR. */
	public static final Setting<File> MODEL_DIR = new Setting<>("incremental.storage.model_dir", DIRECTORY, false,
			"./incremental/model-storage", "TODO");

	/** The Constant MODEL_TAG_FOR_ANALYSIS. */
	public static final Setting<String> MODEL_REVISION_FOR_ANALYSIS = new Setting<>("incremental.analysis_model_revision", STRING,
			true, null, "TODO");

	/** The Constant MODEL_TAG_FOR_REFERENCE. */
	public static final Setting<String> MODEL_REVISION_FOR_REFERENCE = new Setting<>("incremental.reference_model_revision",
			STRING, true, null, "TODO");

	/**
	 * Different types of storage.
	 */
	public static enum VersioningType {

		/** Flat file setting. This means tags are defined by subfolders included in the root folder. For example the tag
		 * "xyz" would assume a folder "root-dir/xyz/" inside of which all required files are included.
		 * For many revisions (= many tagged versions) this is very storage intensive  */
		FLAT,
		
		/** Git setting. This means tags are defined by either commit-hash or tag in a git repository.
		 * Less storage intensive than FLAT.  */
		GIT,
	}
	

	public static enum MergeStrategy {
		FILE_TO_FILE,
		EXTRACTED_ONLY,
	}
	
	/** The Constant STORAGE_TYPE. Defines the form in which the model files are accessed and stored.*/
	public static final Setting<MergeStrategy> MERGE_STRATEGY = new EnumSetting<MergeStrategy>("incremental.merge_strategy",
			MergeStrategy.class, true, null, "TODO");

	/** The Constant STORAGE_TYPE. Defines the form in which the model files are accessed and stored.*/
	public static final Setting<VersioningType> STORAGE_TYPE = new EnumSetting<VersioningType>("incremental.storage.type",
			VersioningType.class, true, null, "TODO");
	
	/** The Constant SOUCE_TYPE. Defines the form in which the source files are accessed.*/
	public static final Setting<VersioningType> SOURCE_TYPE = new EnumSetting<VersioningType>("incremental.source.type",
			VersioningType.class, true, null, "TODO");

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
