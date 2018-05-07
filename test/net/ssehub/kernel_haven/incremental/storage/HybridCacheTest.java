package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.incremental.util.FolderUtil;

public class HybridCacheTest extends HybridCache {

	private static final File TESTFOLDER_HYBRID_DELETE = new File("testdata/hybrid-cache/hybrid-delete");

	@Test
	public void testHybridAdd() throws IOException {
		Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

		HybridCache cache = new HybridCache(tempFolder.toFile());

		cache.hybridAdd(new File("test.txt"));

		Assert.assertThat(FolderUtil.listRelativeFiles(tempFolder.toFile(), true),
				CoreMatchers.hasItem(new File("history/added/test.txt")));
	}

	@Test
	public void testHybridDelete() throws IOException {
		Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

		// Setup
		FolderUtil.copyFolderContent(TESTFOLDER_HYBRID_DELETE, tempFolder.toFile());
		HybridCache cache = new HybridCache(tempFolder.toFile());

		cache.hybridDelete(new File("test.file"));

		Assert.assertThat(FolderUtil.listRelativeFiles(tempFolder.toFile(), true),
				CoreMatchers.hasItem(new File("history/replaced/test.file")));
	}

}
