package net.ssehub.kernel_haven.incremental.diff.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.util.FileUtil;

/**
 * The Class LineCounter.
 * 
 * @author Moritz
 */
public class LineCounter {

    /** The Constant DIFF_START_PATTERN. */
    private static final String DIFF_START_PATTERN = "diff --git ";

    /** The Constant INCLUDE_FILE_TYPES. */
    private static final String[] INCLUDE_FILE_TYPES = {".c", ".h" };

    /** The Constant LINE_NUMBER_MATCH_PATTERN. */
    private static final String LINE_NUMBER_MATCH_PATTERN =
        "@@\\s*-(\\d*),\\d*\\s*\\+\\d*,\\d*\\s*@@";

    /** The line changes for file path. */
    private Map<Path, List<Lines>> lineChangesForFilePath =
        new HashMap<Path, List<Lines>>();

    /**
     * The Class Lines.
     */
    protected static class Lines {
        /** The count. */
        private int count;

        /** The type. */
        private LineType type;

        /**
         * Instantiates a new lines.
         *
         * @param type
         *            the type
         * @param count
         *            the count
         */
        public Lines(LineType type, int count) {
            this.type = type;
            this.count = count;
        }

        /**
         * To string.
         *
         * @return the string
         */
        public String toString() {
            return "Lines [count=" + count + ", type=" + type + "]";
        }

        /**
         * The Enum LineType.
         */
        public enum LineType {

            /** The added. */
            ADDED,
            /** The deleted. */
            DELETED,
            /** The unmodified. */
            UNMODIFIED,
            /** The between chunks. */
            BETWEEN_CHUNKS
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public LineType getType() {
            return type;
        }

        /**
         * Gets the count.
         *
         * @return the count
         */
        public int getCount() {
            return count;
        }

    }

    /**
     * Instantiates a new line counter.
     *
     * @param gitDiffFile
     *            the git diff file
     * @param ignorePaths
     *            the ignore paths
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public LineCounter(File gitDiffFile, Collection<Path> ignorePaths)
        throws IOException {
        parseLines(gitDiffFile, ignorePaths);
    }

    /**
     * Parses the lines.
     *
     * @param commitFile
     *            the commit file
     * @param ignorePaths
     *            the ignore paths
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void parseLines(File commitFile, Collection<Path> ignorePaths)
        throws IOException {
        try (BufferedReader br =
            new BufferedReader(new FileReader(commitFile))) {

            String currentLine = br.readLine();
            // Prevent nextLine from leaking out of scope of the loop
            // CHECKSTYLE:OFF
            for (String nextLine; (nextLine = br.readLine()) != null;) {
                // CHECKSTYLE:ON
                if (currentLine.startsWith(DIFF_START_PATTERN)) {
                    String filePathString = currentLine.substring(
                        currentLine.indexOf("a/") + "a/".length(),
                        currentLine.indexOf(" b/"));
                    Path filePath = Paths.get(filePathString);
                    if (!ignorePaths.contains(filePath)
                        && FileUtil.fileMatchesSuffix(filePath.toFile(),
                            INCLUDE_FILE_TYPES)
                        && !nextLine.startsWith("deleted file mode")
                        && !nextLine.startsWith("new file mode")) {
                        // Skip ahead until next diffEntry or end of file.
                        // build string with lines to parse line-info from.
                        boolean changeBlockFinished = false;
                        StringJoiner changeBlock = new StringJoiner("\n");
                        while (!changeBlockFinished) {
                            changeBlock.add(currentLine);
                            if (nextLine != null
                                && !nextLine.startsWith(DIFF_START_PATTERN)) {
                                currentLine = nextLine;
                                nextLine = br.readLine();
                            } else {
                                changeBlockFinished = true;
                            }
                        }
                        parseChangeBlock(changeBlock.toString());
                    }

                }
                currentLine = nextLine;
            }

        }
    }

    /**
     * Parses the change block.
     *
     * @param string
     *            the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    // CHECKSTYLE:OFF
    protected void parseChangeBlock(String string) throws IOException {
        // CHECKSTYLE:ON
        BufferedReader bufReader = new BufferedReader(new StringReader(string));

        String currentLine = bufReader.readLine();
        String filePathString =
            currentLine.substring(currentLine.indexOf("a/") + "a/".length(),
                currentLine.indexOf(" b/"));
        bufReader.close();
        bufReader = new BufferedReader(new StringReader(string));
        Lines.LineType type = null;
        int typeCounter = 0;
        Path filePath = Paths.get(filePathString);
        lineChangesForFilePath.put(filePath, new ArrayList<Lines>());
        int endOfChunk = 0;
        boolean firstChunkFound = false;
        int lengthOfCurrentChunk = 0;
        while ((currentLine = bufReader.readLine()) != null) {
            // start with first chunk describing line changes, skip until then
            if (!firstChunkFound && !currentLine.startsWith("@@")) {
                continue;
            } else {
                firstChunkFound = true;
            }

            if (type == null) {
                type = Lines.LineType.UNMODIFIED;
            }
            if (currentLine.startsWith("+")) {
                if (!type.equals(Lines.LineType.ADDED)) {
                    lineChangesForFilePath.get(filePath)
                        .add(new Lines(type, typeCounter));
                    typeCounter = 0;
                    type = Lines.LineType.ADDED;
                }
                typeCounter++;
            } else if (currentLine.startsWith("-")) {
                if (!type.equals(Lines.LineType.DELETED)) {
                    lineChangesForFilePath.get(filePath)
                        .add(new Lines(type, typeCounter));
                    typeCounter = 0;
                    type = Lines.LineType.DELETED;
                    lengthOfCurrentChunk++;
                }
                typeCounter++;
            } else if (!currentLine.startsWith("@@")) {
                if (!type.equals(Lines.LineType.UNMODIFIED)) {
                    lineChangesForFilePath.get(filePath)
                        .add(new Lines(type, typeCounter));
                    typeCounter = 0;
                    type = Lines.LineType.UNMODIFIED;
                    lengthOfCurrentChunk++;
                }
                typeCounter++;
            } else if (currentLine.startsWith("@@")) {
                if (typeCounter > 0) {
                    lineChangesForFilePath.get(filePath)
                        .add(new Lines(type, typeCounter));
                }
                Pattern pattern = Pattern.compile(LINE_NUMBER_MATCH_PATTERN);
                Matcher matcher = pattern.matcher(currentLine);
                matcher.find();

                int startNewChunk = Integer.parseInt(matcher.group(1));
                int betweenChunks = startNewChunk - endOfChunk;

                endOfChunk = startNewChunk + lengthOfCurrentChunk;
                lengthOfCurrentChunk = 0;

                lineChangesForFilePath.get(filePath).add(
                    new Lines(Lines.LineType.BETWEEN_CHUNKS, betweenChunks));

                typeCounter = 0;
                type = Lines.LineType.UNMODIFIED;
            }
        }
        if (typeCounter > 0) {
            lineChangesForFilePath.get(filePath)
                .add(new Lines(type, typeCounter));
        }
        bufReader.close();
    }

}
