/*
 * 
 */
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
        return Files.readAllLines(fileA.toPath()).equals(Files.readAllLines(fileB.toPath()));
    }

    /**
     * Read file content.
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
     * Write file with content.
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

    /**
     * Check if path matches any of the provided suffixes.
     *
     * @param file the file
     * @param suffixes the suffixes
     * @return true, if successful
     */
    public static boolean fileMatchesSuffix(File file, String[] suffixes) {

        
        boolean matches = false;
        int i = 0;
        while (i < suffixes.length && !matches) {
            matches = file.getPath().endsWith(suffixes[i]);
            i++;
        }
        return matches;
    }
}
