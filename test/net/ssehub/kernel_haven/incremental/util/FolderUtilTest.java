package net.ssehub.kernel_haven.incremental.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class FolderUtilTest {

	private static final File FOLDER_01_HELLO_WORLD = new File("./testdata/util/folder01/HelloWorld.c");
	private static final File FOLDER_01_HELLO_WORLD_2 = new File("./testdata/util/folder01/HelloWorld2.c");
	private static final File FOLDER_02_HELLO_WORLD = new File("./testdata/util/folder02/HelloWorld.c");
	private static final File FOLDER_03_HELLO_WORLD = new File("./testdata/util/folder03/HelloWorld.c");
	private static final File FOLDER_01 = new File("./testdata/util/folder01/");
	private static final File FOLDER_02 = new File("./testdata/util/folder02/");
	private static final File FOLDER_03 = new File("./testdata/util/folder03/");
	private static final File ROOT_FOLDER = new File("./testdata/util/");

	@Test
	public void testListFilesInDirectory() throws IOException {

		// Precondition: Check if files exist
		assertThat(FOLDER_01_HELLO_WORLD.exists(), is(true));
		assertThat(FOLDER_01_HELLO_WORLD_2.exists(), is(true));
		assertThat(FOLDER_02_HELLO_WORLD.exists(), is(true));
		assertThat(FOLDER_03_HELLO_WORLD.exists(), is(true));

		Collection<File> allFilesInRootFolder = FolderUtil.listFiles(ROOT_FOLDER, true);

		// Check if all and only the files within the directories are included
		assertThat(allFilesInRootFolder.size(), CoreMatchers.is(4));
		assertThat(allFilesInRootFolder, CoreMatchers.hasItems(FOLDER_01_HELLO_WORLD, FOLDER_01_HELLO_WORLD_2,
				FOLDER_02_HELLO_WORLD, FOLDER_03_HELLO_WORLD));

		Collection<File> filesDirectlyInRootFolder = FolderUtil.listFiles(ROOT_FOLDER, false);

		// Check the resulting list is empty as the root folder does not contain any
		// files but only folders
		assertThat(filesDirectlyInRootFolder.size(), CoreMatchers.is(0));

		Collection<File> filesDirectlyInFolder01 = FolderUtil.listFiles(FOLDER_01, false);
		assertThat(filesDirectlyInFolder01.size(), CoreMatchers.is(2));
		assertThat(allFilesInRootFolder, CoreMatchers.hasItems(FOLDER_01_HELLO_WORLD, FOLDER_01_HELLO_WORLD_2));
	}

	@Test
	public void testGetNewOrChangedFiles() throws IOException {

		// Precondition: Check if files exist (the folders in which they exist are
		// indirectly checked as a consequence)
		assertThat(FOLDER_01_HELLO_WORLD.exists(), is(true));
		assertThat(FOLDER_01_HELLO_WORLD_2.exists(), is(true));
		assertThat(FOLDER_02_HELLO_WORLD.exists(), is(true));
		assertThat(FOLDER_03_HELLO_WORLD.exists(), is(true));

		// Test added file
		Collection<File> changeSet = FolderUtil.getNewOrChangedFiles(FOLDER_02, FOLDER_01);
		assertThat(changeSet.size(), CoreMatchers.is(1));
		File changedFile = FOLDER_01.toPath().relativize(FOLDER_01_HELLO_WORLD_2.toPath()).toFile();
		assertThat(changeSet, CoreMatchers.hasItems(changedFile));
		
		// Test deleted file (deletions in newDirectory should not show up)
		changeSet = FolderUtil.getNewOrChangedFiles(FOLDER_01, FOLDER_02);
		assertThat(changeSet.size(), CoreMatchers.is(0));
		
		// Test modified file
		changeSet = FolderUtil.getNewOrChangedFiles(FOLDER_01, FOLDER_03);
		assertThat(changeSet.size(), CoreMatchers.is(1));
		changedFile = FOLDER_03.toPath().relativize(FOLDER_03_HELLO_WORLD.toPath()).toFile();
		assertThat(changeSet, CoreMatchers.hasItems(changedFile));


	}
}
