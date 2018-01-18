package net.ssehub.kernel_haven.incremental.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;


public class FileUtilTest extends FileUtil {
	
	private static final File HELLO_WORLD = new File("./testdata/util/folder01/HelloWorld.c");
	private static final File HELLO_WORLD_2 = new File("./testdata/util/folder01/HelloWorld2.c");
	private static final File FOLDER_01 = new File("./testdata/util/folder01/");
	private static final File FOLDER_02 = new File("./testdata/util/folder02/");
	private static final File FOLDER_03 = new File("./testdata/util/folder03/");

	@Test
	public void testFileEquals() throws IOException {
		assertThat(HELLO_WORLD.exists(), is(true));
		assertThat(fileContentIsEqual(HELLO_WORLD, HELLO_WORLD),is(true));
		
		assertThat(HELLO_WORLD_2.exists(), is(true));
		assertThat(fileContentIsEqual(HELLO_WORLD, HELLO_WORLD_2),is(false));
	}
	
}
