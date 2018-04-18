package net.ssehub.kernel_haven.incremental.preparation;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.incremental.preparation.filter.DefaultFilter;
import net.ssehub.kernel_haven.incremental.settings.IncrementalAnalysisSettings;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;

// TODO: Auto-generated Javadoc
/**
 * The Class IncrementalPreparationTest.
 * @author moritz
 */
public class IncrementalPreparationTest extends IncrementalPreparation {

	/** The logger. */
	private static Logger LOGGER = null;
	
	/** The Constant MODIFIED_FOLDER. */
	private static final File MODIFIED_FOLDER = new File("testdata/incremental-pipeline/modified");
	
	/** The Constant DIFF_FILE. */
	private static final File DIFF_FILE = new File("testdata/incremental-pipeline/git.diff");

	/**
	 * Inits the logger.
	 */
	@BeforeClass
	public static void initLogger() {
		LOGGER = Logger.get();
		LOGGER.setLevel(Level.DEBUG);

	}

	/**
	 * Tests whether the filterInput method works.
	 * 
	 * 
	 */
	@Test
	public void testFilterInput() {
		try {
			this.filterInput(DefaultFilter.class.getName(), MODIFIED_FOLDER, DIFF_FILE, Pattern.compile(".*"));
		} catch (SetUpException e) {
			Assert.fail("the filterInput method did not terminate properly: " + e.getMessage());
		}
	}

	/**
	 * Test run.
	 *
	 * @throws SetUpException the set up exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRun() throws SetUpException, IOException {

		Path tempFolderPath = Files.createTempDirectory("incremental-analysis-test-run");
		File tempFolder = tempFolderPath.toFile();
		LOGGER.logDebug("Temp-Folder for testRun: " + tempFolder);

		FolderUtil.copyFolderContent(MODIFIED_FOLDER, tempFolder);

		Properties prop = new Properties();

		prop.setProperty(IncrementalAnalysisSettings.CODE_MODEL_FILTER_CLASS.getKey(), DefaultFilter.class.getName());
		prop.setProperty(IncrementalAnalysisSettings.BUILD_MODEL_FILTER_CLASS.getKey(), DefaultFilter.class.getName());
		prop.setProperty(IncrementalAnalysisSettings.VARIABILITY_MODEL_FILTER_CLASS.getKey(),
				DefaultFilter.class.getName());
		prop.setProperty(IncrementalAnalysisSettings.HYBRID_CACHE_DIRECTORY.getKey(), tempFolder.getAbsolutePath());

		prop.setProperty(IncrementalAnalysisSettings.SOURCE_TREE_DIFF_FILE.getKey(), DIFF_FILE.getAbsolutePath());
		prop.setProperty(DefaultSettings.SOURCE_TREE.getKey(), tempFolder.getAbsolutePath());

		// The following parameters are not used actively in the Preparation but are
		// marked as mandatory in {@link #DefaultSettings}
		prop.setProperty(DefaultSettings.ANALYSIS_CLASS.getKey(), "NOT USED");
		prop.setProperty(DefaultSettings.CACHE_DIR.getKey(), tempFolder.getAbsolutePath());
		prop.setProperty(DefaultSettings.OUTPUT_DIR.getKey(), tempFolder.getAbsolutePath());
		prop.setProperty(DefaultSettings.RESOURCE_DIR.getKey(), tempFolder.getAbsolutePath());
		prop.setProperty(DefaultSettings.PLUGINS_DIR.getKey(), tempFolder.getAbsolutePath());

		Configuration config = new Configuration(prop);

		IncrementalAnalysisSettings.registerAllSettings(config);
		DefaultSettings.registerAllSettings(config);

		IncrementalPreparation preparation = new IncrementalPreparation();
		preparation.run(config);

		List<String> listOfFilesForCodeModel = config.getValue(DefaultSettings.CODE_EXTRACTOR_FILES);

		Assert.assertThat(listOfFilesForCodeModel,
				CoreMatchers.anyOf(CoreMatchers.hasItems("a-code-file.c", "modify/a-code-file.c"),
						CoreMatchers.hasItems("a-code-file.c", "modify\\a-code-file.c")));

		Assert.assertThat(config.getValue(IncrementalAnalysisSettings.EXTRACT_BUILD_MODEL), equalTo(Boolean.TRUE));
		Assert.assertThat(config.getValue(IncrementalAnalysisSettings.EXTRACT_VARIABILITY_MODEL),
				equalTo(Boolean.TRUE));
		Assert.assertThat(config.getValue(IncrementalAnalysisSettings.EXTRACT_CODE_MODEL), equalTo(Boolean.TRUE));

	}

}
