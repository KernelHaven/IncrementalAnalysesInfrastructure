package net.ssehub.kernel_haven.incremental.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

// TODO: Auto-generated Javadoc
/**
 * Utility class for files.
 * 
 * @author Moritz
 */
public class FileUtil {

    /**
     * Checks whether file content is equal.
     *
     * @param fileA
     *            the file A
     * @param fileB
     *            the file B
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean fileContentIsEqual(File fileA, File fileB)
        throws IOException {
        byte[] otherBytes = Files.readAllBytes(fileA.toPath());
        byte[] thisBytes = Files.readAllBytes(fileB.toPath());
        return Arrays.equals(otherBytes, thisBytes);
    }

    /**
     * Read file.
     *
     * @param file
     *            the file
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Write file.
     *
     * @param file
     *            the file
     * @param content
     *            the content
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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
