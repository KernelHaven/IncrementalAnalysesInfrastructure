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





	public static final Setting INPUT_DIFF_FILE = null;
	public static final Setting FILES_STORAGE_DIR = null;

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
