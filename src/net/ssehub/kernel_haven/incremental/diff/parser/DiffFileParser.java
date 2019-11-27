package net.ssehub.kernel_haven.incremental.diff.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.FileChange;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.Lines;
import net.ssehub.kernel_haven.incremental.diff.parser.FileEntry.VariabilityChange;
import net.ssehub.kernel_haven.incremental.util.PosixUtil;
import net.ssehub.kernel_haven.util.Logger;

/**
 * {@link net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParser} is
 * used to extract change information from a given git diff file.
 * 
 * @author Moritz
 */
public class DiffFileParser {

    /** The Constant DIFF_START_PATTERN. */
    private static final String DIFF_START_PATTERN = "diff --git ";

    /** The Constant LINE_NUMBER_MATCH_PATTERN. */
    private static final String LINE_NUMBER_MATCH_PATTERN = "@@\\s*-(\\d*),?\\d*\\s*\\+\\d*,?\\d*\\s*@@(.*)";

    /** The Constant GIT_BINARY_PATCH_START_PATTERN. */
    private static final String GIT_BINARY_PATCH_START_PATTERN = "GIT binary patch";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.get();

    /**
     * Blocks public constructor access.
     */
    private DiffFileParser() {

    }

    /**
     * Parses a given git diff file to a
     * {@link net.ssehub.kernel_haven.incremental.diff.parser.DiffFile} object.
     * Unifies merges consisting of a deletion entry and a addition entry into a
     * single merge entry. Skips entries for binary patches.
     *
     * @param commitFile the commit file
     * @return the diff file
     */
    // CHECKSTYLE:OFF
    public static DiffFile parse(File commitFile) {
        // CHECKSTYLE:ON
        DiffFile diffFile = null;
        try (BufferedReader br = new BufferedReader(new FileReader(commitFile))) {

            // Reading four lines in total, as the fourth line contains
            // information on whether the diff for the file was a binary diff
            String currentLine = br.readLine();
            String nextLine1 = br.readLine();
            String nextLine2 = br.readLine();
            String nextLine3 = br.readLine();

            // This is instanciated as a list because sometimes a modification is
            // split into deletion and subsequent addition of a file within a diff file.
            // To cover this case we check (and if needed modify)
            // the previous entry which requires an ordered list.
            List<FileEntry> fileEntries = new ArrayList<>();

            // An outer loop finds the beginning of entries within the diff file
            // An inner loop then processes the entries to extract information
            // about which lines changed within the file described bty the entry
            while (nextLine1 != null && currentLine != null) {

                // find beginnning of file change description within diff file, skip binary
                // patches
                // CHECKSTYLE:OFF
                if (currentLine.startsWith(DIFF_START_PATTERN) && nextLine3 != null
                        && !nextLine3.startsWith(GIT_BINARY_PATCH_START_PATTERN) && nextLine2 != null
                        && !nextLine2.startsWith(GIT_BINARY_PATCH_START_PATTERN)
                        && !nextLine1.startsWith(GIT_BINARY_PATCH_START_PATTERN)) {
                    // CHECKSTYLE:ON
                    Set<PosixFilePermission> permissions = new HashSet<>();
                    String filePathString = currentLine.substring(currentLine.indexOf("a/") + "a/".length(),
                            currentLine.indexOf(" b/"));
                    FileEntry.FileChange type;

                    Path filePath = Paths.get(filePathString);

                    if (nextLine1.startsWith("new file mode")) {
                        type = FileEntry.FileChange.ADDITION;
                        String posixFlag = nextLine1.substring("new file mode 10".length());
                        permissions.addAll(PosixUtil.getPosixFilePermissionForNumberString(posixFlag));
                    } else if (nextLine1.startsWith("deleted file mode")) {
                        type = FileEntry.FileChange.DELETION;
                        String posixFlag = nextLine1.substring("deleted file mode 10".length());
                        permissions.addAll(PosixUtil.getPosixFilePermissionForNumberString(posixFlag));
                    } else if (nextLine1.startsWith("index")) {
                        type = FileEntry.FileChange.MODIFICATION;
                        Pattern modificationPosisxFlagPattern = Pattern.compile("index\\s.+\\.+\\S+\\s10(\\d*)");
                        Matcher matcher = modificationPosisxFlagPattern.matcher(nextLine1);
                        if (matcher.find()) {
                            String posixFlag = matcher.group(1);
                            permissions.addAll(PosixUtil.getPosixFilePermissionForNumberString(posixFlag));
                        } else {
                            LOGGER.logError(
                                    "Failed to get posix-flag for file " + filePathString + " in line:" + nextLine1);
                        }
                    } else {
                        type = FileEntry.FileChange.MODIFICATION;
                        LOGGER.logWarning("Unusual pattern in entry for file " + filePathString + ":" + nextLine1);
                    }

                    // Skip ahead until next diffEntry or end of file.
                    // build string with lines to parse line-info from.
                    boolean changeBlockFinished = false;
                    StringJoiner changeBlock = new StringJoiner("\n");
                    while (!changeBlockFinished) {
                        changeBlock.add(currentLine);
                        if (nextLine1 != null && !nextLine1.startsWith(DIFF_START_PATTERN)) {
                            currentLine = nextLine1;
                            nextLine1 = nextLine2;
                            nextLine2 = nextLine3;
                            nextLine3 = br.readLine();
                        } else {
                            changeBlockFinished = true;
                        }
                    }

                    List<Lines> lines = parseChangeBlock(changeBlock.toString());

                    FileEntry previousEntry = null;

                    if (!fileEntries.isEmpty()) {
                        previousEntry = fileEntries.get(fileEntries.size() - 1);
                    }
                    if (type.equals(FileChange.ADDITION) && previousEntry != null
                            && previousEntry.getType().equals(FileChange.DELETION)
                            && previousEntry.getPath().equals(filePath)) {
                        previousEntry.setType(FileChange.MODIFICATION);
                        previousEntry.setPermissions(permissions);
                        previousEntry.addLines(lines);
                        previousEntry.setNoNewLineAtEndOfFile(
                                changeBlock.toString().endsWith("\\ No newline at end of file"));
                    } else {
                        FileEntry entry =
                                new FileEntry(filePath, type, VariabilityChange.NOT_ANALYZED, lines, permissions);
                        entry.setNoNewLineAtEndOfFile(changeBlock.toString().endsWith("\\ No newline at end of file"));
                        fileEntries.add(entry);
                    }
                }
                currentLine = nextLine1;
                nextLine1 = nextLine2;
                nextLine2 = nextLine3;
                nextLine3 = br.readLine();
                diffFile = new DiffFile(fileEntries);
            }
        } catch (IOException exc) {
            LOGGER.logException("Could not parse git diff file", exc);
            diffFile = null;
        }
        return diffFile;
    }

    /**
     * Parses the change block.
     *
     * @param string the string
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    // CHECKSTYLE:OFF
    private static List<Lines> parseChangeBlock(String string) throws IOException {
        // CHECKSTYLE:ON
        BufferedReader bufReader = new BufferedReader(new StringReader(string));

        List<Lines> lineChanges = new ArrayList<>();
        String currentLine = null;
        Lines.LineType type = null;

        StringJoiner chunkContent = new StringJoiner("\n");
        int typeCounter = 0;

        int endOfChunk = 1;
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
                    if (typeCounter > 0) {
                        lineChanges.add(new Lines(type, typeCounter, chunkContent.toString()));
                    }
                    chunkContent = new StringJoiner("\n");
                    typeCounter = 0;
                    type = Lines.LineType.ADDED;
                }
                chunkContent.add(currentLine.substring(1));
                typeCounter++;
            } else if (currentLine.startsWith("-")) {
                if (!type.equals(Lines.LineType.DELETED)) {
                    if (typeCounter > 0) {
                        lineChanges.add(new Lines(type, typeCounter, chunkContent.toString()));
                    }
                    chunkContent = new StringJoiner("\n");
                    typeCounter = 0;
                    type = Lines.LineType.DELETED;
                }
                chunkContent.add(currentLine.substring(1));
                endOfChunk++;
                typeCounter++;
            } else if (currentLine.startsWith(" ")) {
                if (!type.equals(Lines.LineType.UNMODIFIED)) {
                    if (typeCounter > 0) {
                        lineChanges.add(new Lines(type, typeCounter, chunkContent.toString()));
                    }
                    chunkContent = new StringJoiner("\n");
                    typeCounter = 0;
                    type = Lines.LineType.UNMODIFIED;
                }
                chunkContent.add(currentLine.substring(1));
                endOfChunk++;
                typeCounter++;
            } else if (currentLine.startsWith("@@")) {
                // This handles lines starting with @@
                // Lines starting with @@ mark the start of a new block of
                // changes / chunk

                // add the previously collected Lines to the list
                if (typeCounter > 0) {
                    lineChanges.add(new Lines(type, typeCounter, chunkContent.toString()));
                    chunkContent = new StringJoiner("\n");
                }

                // Find the start of the new block of changes / chunk
                Pattern pattern = Pattern.compile(LINE_NUMBER_MATCH_PATTERN);
                Matcher matcher = pattern.matcher(currentLine);
                matcher.find();
                String numberString = matcher.group(1);
                int startNewChunk = 1;
                // Take line - 1 as start of the new chunk except for blocks that start in the first line.
                // This is because all chunks except for those at the very start of the file repeat the last line
                // before the actual start of the chunk.
                if (!numberString.isEmpty() && !numberString.equals("1")) {
                    startNewChunk = Integer.parseInt(matcher.group(1)) - 1;
                }

                // Add the space between the previous block of changes / between
                // chunks
                if (startNewChunk - endOfChunk != 0) {
                    lineChanges.add(new Lines(Lines.LineType.BETWEEN_CHUNKS, startNewChunk - endOfChunk,
                            chunkContent.toString()));
                }
                chunkContent = new StringJoiner("\n");

                // Reset the end of chunk. endOfChunk will be modified while
                // processing the current chunk so that it matches
                // the actual end of the chunk when the next line starting with
                // @@ is found.
                endOfChunk = startNewChunk + 1;

                typeCounter = 0;
                type = Lines.LineType.UNMODIFIED;
                if (!matcher.group(2).isEmpty()) {
                    chunkContent.add(matcher.group(2));
                    typeCounter++;
                }
            }
        }
        if (typeCounter > 0) {
            lineChanges.add(new Lines(type, typeCounter, chunkContent.toString()));
        }

        bufReader.close();
        return lineChanges;
    }

}
