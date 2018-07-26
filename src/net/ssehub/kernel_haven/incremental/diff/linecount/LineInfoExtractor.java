package net.ssehub.kernel_haven.incremental.diff.linecount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class {@link LineParser}.
 * 
 * @author Moritz
 */
public class LineInfoExtractor {

    /** The Constant DIFF_START_PATTERN. */
    private static final String DIFF_START_PATTERN = "diff --git ";

    /** The Constant LINE_NUMBER_MATCH_PATTERN. */
    private static final String LINE_NUMBER_MATCH_PATTERN =
        "@@\\s*-(\\d*),\\d*\\s*\\+\\d*,\\d*\\s*@@";

    /** The line changes for file path. */
    private Map<Path, List<Lines>> lineChangesForFilePath =
        new HashMap<Path, List<Lines>>();

    /**
     * Instantiates a new line counter.
     *
     * @param gitDiffFile
     *            the git diff file
     * @param ignorePaths
     *            the ignore paths
     * @param fileInclusionRegex
     *            the file inclusion regex
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public LineInfoExtractor(File gitDiffFile, Collection<Path> ignorePaths,
        Pattern fileInclusionRegex) throws IOException {
        parseLines(gitDiffFile, ignorePaths, fileInclusionRegex);
    }

    /**
     * Instantiates a new line counter.
     *
     * @param gitDiffFile
     *            the git diff file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public LineInfoExtractor(File gitDiffFile) throws IOException {
        parseLines(gitDiffFile, null, Pattern.compile(".*"));
    }

    /**
     * Gets the lines.
     *
     * @param path
     *            the path
     * @return the lines
     */
    public List<Lines> getLines(Path path) {
        return lineChangesForFilePath.get(path);
    }

    /**
     * Parses the lines.
     *
     * @param commitFile
     *            the commit file
     * @param ignorePaths
     *            the ignore paths
     * @param fileInclusionRegex
     *            the file inclusion regex
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void parseLines(File commitFile, Collection<Path> ignorePaths,
        Pattern fileInclusionRegex) throws IOException {
        if (ignorePaths == null) {
            ignorePaths = new ArrayList<Path>();
        }

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
                        && fileInclusionRegex.matcher(filePathString).matches()
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
    private void parseChangeBlock(String string) throws IOException {
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
                }
                endOfChunk++;
                typeCounter++;
            } else if (!currentLine.startsWith("@@")) {
                if (!type.equals(Lines.LineType.UNMODIFIED)) {
                    lineChangesForFilePath.get(filePath)
                        .add(new Lines(type, typeCounter));
                    typeCounter = 0;
                    type = Lines.LineType.UNMODIFIED;
                }
                endOfChunk++;
                typeCounter++;
            } else {
                // This handles lines starting with @@
                // Lines starting with @@ mark the start of a new block of
                // changes / chunk

                // add the previously collected Lines to the list
                if (typeCounter > 0) {
                    lineChangesForFilePath.get(filePath)
                        .add(new Lines(type, typeCounter));
                }

                // Find the start of the new block of changes / chunk
                Pattern pattern = Pattern.compile(LINE_NUMBER_MATCH_PATTERN);
                Matcher matcher = pattern.matcher(currentLine);
                matcher.find();
                int startNewChunk = Integer.parseInt(matcher.group(1));

                // Add the space between the previous block of changes / between
                // chunks
                lineChangesForFilePath.get(filePath).add(new Lines(
                    Lines.LineType.BETWEEN_CHUNKS, startNewChunk - endOfChunk));

                // Reset the end of chunk. endOfChunk will be modified while
                // processing the current chunk so that it matches
                // the actual end of the chunk when the next line starting with
                // @@ is found.
                endOfChunk = startNewChunk;

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

    /**
     * The Class Lines.
     */
    public static class Lines {
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

}
