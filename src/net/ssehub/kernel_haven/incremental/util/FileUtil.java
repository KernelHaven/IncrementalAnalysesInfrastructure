package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileUtil {

	public static boolean fileContentIsEqual(File file01, File file02) throws IOException {
		byte[] otherBytes = Files.readAllBytes(file01.toPath());
		byte[] thisBytes = Files.readAllBytes(file02.toPath());
		return Arrays.equals(otherBytes, thisBytes);
	}

	public static String getHash(File file) throws IOException {
		MessageDigest md;
		String result = null;
		try {
			md = MessageDigest.getInstance("SHA-256");

			try (InputStream is = Files.newInputStream(file.toPath());
					DigestInputStream dis = new DigestInputStream(is, md)) {
			}
			byte[] digest = md.digest();
			result = new String(digest);
		} catch (NoSuchAlgorithmException e) {
			// Never happens
		}

		return result;
	}
}
