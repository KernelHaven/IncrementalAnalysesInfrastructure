package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Utility class for files.
 * 
 * @author Moritz
 */
public class FileUtil {

	/**
	 * Checks if the content of two files is equal.
	 *
	 * @param file01
	 *            the file 01
	 * @param file02
	 *            the file 02
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean fileContentIsEqual(File file01, File file02) throws IOException {
		byte[] otherBytes = Files.readAllBytes(file01.toPath());
		byte[] thisBytes = Files.readAllBytes(file02.toPath());
		return Arrays.equals(otherBytes, thisBytes);
	}

	/**
	 * Gets the SHA-256 Hash of a file.
	 *
	 * @param file
	 *            the file
	 * @return the hash
	 * @throws IOException
	 *             occurs if file is not present or can not be accessed.
	 */
	public static String getSha256Hash(File file) throws IOException {
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

	public static String readFile(File file) throws IOException {
		StringBuilder fileContents = new StringBuilder();
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");
		try {
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}

	public static void writeFile(File file, String content) throws IOException {
		if (file.exists()) {
			file.delete();
		}

		file.createNewFile();

		try (PrintWriter out = new PrintWriter(file)) {
			out.println(content);
		}
	}
}
