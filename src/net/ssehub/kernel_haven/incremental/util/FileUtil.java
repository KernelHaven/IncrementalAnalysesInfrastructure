package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class FileUtil {

	public static boolean fileContentIsEqual(File file01, File file02) throws IOException {
		byte[] otherBytes = Files.readAllBytes(file01.toPath());
		byte[] thisBytes = Files.readAllBytes(file02.toPath());
		return Arrays.equals(otherBytes, thisBytes);
	}
	
}
