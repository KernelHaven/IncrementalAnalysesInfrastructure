package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.build_model.BuildModelCache;
import net.ssehub.kernel_haven.code_model.CodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityModelCache;

/**
 * {@link HybridCache} serves the purpose of storing two different versions of
 * the models. Starting off with the previously present model (an empty model is
 * also possible), one can add or delete elements within that model. When a
 * modification takes place, the original data is persisted along with the
 * modification allowing for continuous access to the previous model. As
 * {@link HybridCache} only stores two models, clearing information for the
 * previous model via {@link HybridCache#clearChangeHistory()} offers you the
 * option to clear the change-history thereby resulting in the previous model
 * being equivalent to the current model at the time of the method-call (as all
 * history-information is wiped you are left with the current model and no
 * history). Any subsequent modifications will then again be stored within the
 * history.
 * 
 * @author moritz
 * 
 */
public class HybridCache {

    /**
     * Stores cache-files for the current models.
     */
    private static final Path CURRENT_CACHE_FOLDER = Paths.get("current/");

    /**
     * As filenames for the CodeModel-cache are generated out of the paths of
     * source-files, this constant defines the chars to replace in the
     * source-file-path.
     */
    private static final char CM_REPLACE_THIS = File.separatorChar;

    /**
     * As filenames for the CodeModel-cache are generated out of the paths of
     * source-files, this constant defines the replacement for chars defined in
     * {@link HybridCache#CM_REPLACE_THIS for the source-file-path.
     */
    private static final char CM_REPLACEMENT = '.';

    /**
     * Suffix for cache-files of the cm-cache.
     */
    private static final String CM_CACHE_SUFFIX = ".cache";

    /**
     * The folder represented by this path stores cache-files that replaced
     * files in the current model. Those files can be used to access the
     * previous model.
     */
    private static final Path REPLACED_FOLDER = Paths.get("history/backup/");

    /** The Constant FLAG_FOLDER. */
    private static final Path CHANGE_INFORMATION_FOLDER =
        Paths.get("history/change-information/");

    /**
     * The Enum Flag.
     */
    public static enum ChangeFlag {

            /** The auxillary change. */
            AUXILLARY_CHANGE("auxillary"),

            /** The change through extraction. */
            EXTRACTION_CHANGE("extraction"),

            /** The modification. */
            MODIFICATION("modification"),

            /** The addition. */
            ADDITION("addition"),

            /** The deletion. */
            DELETION("deletion");

        /** The flag. */
        private String flag;

        /**
         * Instantiates a new flag.
         *
         * @param flag
         *            the flag
         */
        private ChangeFlag(String flag) {
            this.flag = flag;
        }

        /**
         * To string.
         *
         * @return the string
         */
        public String toString() {
            return this.flag;
        }
    }

    /** Subpaths for all files representing the build-model cache. */
    private static final Path[] BM_CACHE_FILES = { Paths.get("bmCache") };

    /** Subpaths for all files representing the variability-model cache. */
    private static final Path[] VM_CACHE_FILES = { Paths.get("vmCache") };

    /** The current folder. */
    private File currentFolder;

    /** The replaced folder. */
    private File replacedFolder;

    /** The flag folder. */
    private File changeInformationFolder;

    /**
     * Cache-Object for accessing cm-cache elements in
     * {@link HybridCache#currentFolder}.
     */
    private CodeModelCache currentCmCache;

    /**
     * Cache-Object for accessing vm-cache elements in
     * {@link HybridCache#currentFolder}.
     */
    private VariabilityModelCache currentVmCache;

    /**
     * Cache-Object for accessing bm-cache elements in
     * {@link HybridCache#currentFolder}.
     */
    private BuildModelCache currentBmCache;

    /**
     * Cache-Object for accessing cm-cache elements in
     * {@link HybridCache#backup}.
     */
    private CodeModelCache replacedCmCache;

    /**
     * Cache-Object for accessing vm-cache elements in
     * {@link HybridCache#backup}.
     */
    private VariabilityModelCache replacedVmCache;

    /**
     * Cache-Object for accessing bm-cache elements in
     * {@link HybridCache#backup}.
     */
    private BuildModelCache replacedBmCache;

    /**
     * Instantiates a new hybrid cache.
     */
    protected HybridCache() {
        // Empty constructor for JUnit-Tests only
    }

    /**
     * Instantiates a new hybrid cache.
     *
     * @param cacheFolder
     *            the cache folder
     */
    public HybridCache(File cacheFolder) {
        this.currentFolder =
            cacheFolder.toPath().resolve(CURRENT_CACHE_FOLDER).toFile();
        this.replacedFolder =
            cacheFolder.toPath().resolve(REPLACED_FOLDER).toFile();
        this.changeInformationFolder =
            cacheFolder.toPath().resolve(CHANGE_INFORMATION_FOLDER).toFile();
        this.currentFolder.mkdirs();
        this.replacedFolder.mkdirs();
        this.changeInformationFolder.mkdir();
        this.currentBmCache = new BuildModelCache(currentFolder);
        this.currentVmCache = new VariabilityModelCache(currentFolder);
        this.currentCmCache = new CodeModelCache(currentFolder);
        this.replacedCmCache = new CodeModelCache(replacedFolder);
        this.replacedVmCache = new VariabilityModelCache(replacedFolder);
        this.replacedBmCache = new BuildModelCache(replacedFolder);
    }

    /**
     * Gets the cache file name.
     *
     * @param file
     *            the file
     * @return the cache file name
     */
    public static String getCacheFileName(File file) {
        return file.getPath().replace(CM_REPLACE_THIS, CM_REPLACEMENT)
            + CM_CACHE_SUFFIX;
    }

    /**
     * Removes all information for the previous model from cache. Only keeps
     * current model.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void clearChangeHistory() throws IOException {
        FolderUtil.deleteFolderContents(replacedFolder);
        FolderUtil.deleteFolderContents(changeInformationFolder);
    }

    /**
     * Write a {@link SourceFile} to the cache replacing any existing model
     * accessible via {@link HybridCache#readCm()} with the same
     * {@link SourceFile#getPath()}. The previous model can thereafter be
     * accessed through {@link HybridCache#readPreviousCmCacheFile(File)}
     *
     * @param sourceFile
     *            the source file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void write(SourceFile sourceFile) throws IOException {
        String fileNameInCache = getCacheFileName(sourceFile.getPath());
        File newFile = currentFolder.toPath().resolve(fileNameInCache).toFile();
        if (newFile.exists()) {
            Files.move(newFile.toPath(),
                replacedFolder.toPath().resolve(fileNameInCache),
                StandardCopyOption.REPLACE_EXISTING);
            flag(sourceFile, ChangeFlag.MODIFICATION);
        } else {
            flag(sourceFile, ChangeFlag.ADDITION);
        }
        currentCmCache.write(sourceFile);
    }

    /**
     * Flag.
     *
     * @param file
     *            the file
     * @param flag
     *            the flag
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void flag(SourceFile file, ChangeFlag flag) throws IOException {
        String fileNameInCache = getCacheFileName(file.getPath());
        flag(new File(fileNameInCache), flag);
    }

    /**
     * Adds a flag to the SourceFile within the HybridCache. This flag is part
     * of the file-history and should describe changes made to the file in the
     * current iteration. It is possible to assign multiple flags to a file.
     *
     * @param cacheFile
     *            the cache file
     * @param flag
     *            the flag
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void flag(File cacheFile, ChangeFlag flag) throws IOException {
        File flagFile = this.getFlagFile(cacheFile, flag);
        flagFile.getParentFile().mkdirs();
        if (!flagFile.exists()) {
            flagFile.createNewFile();
        }
    }

    private File getFlagFile(File cacheFile, ChangeFlag flag) {
        String fileNameInCache = flag.toString() + "/" + cacheFile.getPath();
        File flagFile =
            changeInformationFolder.toPath().resolve(fileNameInCache).toFile();
        return flagFile;
    }

    /**
     * Write the {@link VariabilityModel} to the cache replacing the model
     * accessible via {@link HybridCache#readVm()}. The previous model can
     * thereafter be accessed through {@link HybridCache#readPreviousVm()}
     *
     * @param vmModel
     *            the vm model to write to the cache.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void write(VariabilityModel vmModel) throws IOException {
        for (Path vmCacheFile : VM_CACHE_FILES) {
            File fileToAdd =
                currentFolder.toPath().resolve(vmCacheFile).toFile();
            if (fileToAdd.exists()) {
                flag(vmCacheFile.toFile(), ChangeFlag.MODIFICATION);
                Files.move(currentFolder.toPath().resolve(vmCacheFile),
                    replacedFolder.toPath().resolve(vmCacheFile),
                    StandardCopyOption.REPLACE_EXISTING);
            } else {
                flag(vmCacheFile.toFile(), ChangeFlag.ADDITION);
            }
        }
        currentVmCache.write(vmModel);
    }

    /**
     * Write the {@link BuildModel} to the cache replacing the model accessible
     * via {@link HybridCache#readBm()}. The previous model can thereafter be
     * accessed through {@link HybridCache#readPreviousBm()}
     *
     * @param buildModel
     *            the build model
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void write(BuildModel buildModel) throws IOException {
        for (Path bmCacheFile : BM_CACHE_FILES) {
            File fileToAdd =
                currentFolder.toPath().resolve(bmCacheFile).toFile();
            if (fileToAdd.exists()) {
                flag(bmCacheFile.toFile(), ChangeFlag.MODIFICATION);
                Files.move(currentFolder.toPath().resolve(bmCacheFile),
                    replacedFolder.toPath().resolve(bmCacheFile),
                    StandardCopyOption.REPLACE_EXISTING);
            } else {
                flag(bmCacheFile.toFile(), ChangeFlag.ADDITION);
            }
        }
        currentBmCache.write(buildModel);
    }

    /**
     * Read cm from a cache file in current version. In contrast to
     * {@link CodeModelCache#read(File)} this expects the name of the cache file
     * instead of the name of the file in the source-tree.
     *
     * @param cacheFile
     *            the target
     * @return the source file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    protected SourceFile readCmCacheFile(File cacheFile)
        throws IOException, FormatException {
        File originalFile = getOriginalCodeModelFile(cacheFile);

        SourceFile srcFile = null;
        if (originalFile != null) {
            srcFile = currentCmCache.read(originalFile);
        }
        return srcFile;
    }

    /**
     * Gets the original file object (File within the source-tree) corresponding
     * to the cached file.
     *
     * @param cachedFile
     *            the cached file
     * @return the original file
     */
    protected File getOriginalCodeModelFile(File cachedFile) {
        String cachedFilePath = cachedFile.getPath();
        String originalFilePath = null;
        File originalFile = null;

        Matcher matcher = Pattern.compile("^([\\S]+)(\\.[^\\.]+)(\\.cache)$")
            .matcher(cachedFilePath);

        if (matcher.find()) {
            if (matcher.groupCount() >= 2) {
                originalFilePath =
                    matcher.group(1).replace('.', '/') + matcher.group(2);
                originalFile = new File(originalFilePath);
            }
        }

        return originalFile;
    }

    /**
     * Read build model in current version.
     *
     * @return the builds the model
     * @throws FormatException
     *             the format exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public BuildModel readBm() throws FormatException, IOException {
        return currentBmCache.read(new File("not-used"));

    }

    /**
     * Read variability model in current version.
     *
     * @return the variability model
     * @throws FormatException
     *             the format exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public VariabilityModel readVm() throws FormatException, IOException {
        return currentVmCache.read(new File("not-used"));

    }

    /**
     * Read code model in previous version from a cache-file.
     * 
     * @param target
     *            File object representing a cache-file.
     * @return the source file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    protected SourceFile readPreviousCmCacheFile(File target)
        throws IOException, FormatException {
        SourceFile result = null;
        // read from replaced folder if file was deleted or got replaced through
        // the
        // current version
        if (replacedFolder.toPath().resolve(target.toPath()).toFile()
            .exists()) {
            result = replacedCmCache.read(getOriginalCodeModelFile(target));

            /*
             * read from current folder if file was not newly added as the file
             * was not touched and remains the same in both the current and
             * previous version
             */
        } else if (currentFolder.toPath().resolve(target.toPath()).toFile()
            .exists() && !(cacheFileHasFlag(target, ChangeFlag.ADDITION))) {
            result = currentCmCache.read(getOriginalCodeModelFile(target));
        }
        return result;

    }

    /**
     * Cache file has flag.
     *
     * @param target
     *            the target
     * @param flag
     *            the flag
     * @return true, if successful
     */
    private boolean cacheFileHasFlag(File target, ChangeFlag flag) {
        return changeInformationFolder.toPath()
            .resolve(flag + "/" + target.getPath()).toFile().exists();
    }

    /**
     * Read build model in previous version.
     *
     * @return the builds the model
     * @throws FormatException
     *             the format exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public BuildModel readPreviousBm() throws FormatException, IOException {
        boolean previousModelExists = existsInReplaced(BM_CACHE_FILES);

        BuildModel result = null;
        if (previousModelExists) {
            result = replacedBmCache.read(new File("bmCache"));
        } else {
            result = currentBmCache.read(new File("bmCache"));
        }
        return result;

    }

    /**
     * Read variability model in previous version.
     *
     * @return the variability model
     * @throws FormatException
     *             the format exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public VariabilityModel readPreviousVm()
        throws FormatException, IOException {
        boolean previousModelExists = existsInReplaced(VM_CACHE_FILES);

        VariabilityModel result = null;
        if (previousModelExists) {
            result = replacedVmCache.read(new File("vmCache"));
        } else {
            result = currentVmCache.read(new File("vmCache"));
        }

        return result;
    }

    /**
     * Checks if the paths exist as subpaths within the replaced-folder.
     *
     * @param paths
     *            the paths
     * @return true, if exists
     */
    protected boolean existsInReplaced(Path[] paths) {
        boolean existsInPrevious = true;
        for (Path path : paths) {
            if (existsInPrevious) {
                existsInPrevious =
                    REPLACED_FOLDER.resolve(path).toFile().exists();
                if (!existsInPrevious) {
                    break;
                }
            }
        }
        return existsInPrevious;
    }

    /**
     * Delete code model for a code-file within the source-tree.
     *
     * @param codeFileWithinSourceTree
     *            the path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteCodeModel(File codeFileWithinSourceTree)
        throws IOException {
        File fileToDelete = currentFolder.toPath()
            .resolve(getCacheFileName(codeFileWithinSourceTree)).toFile();
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    /**
     * Read complete previous code model.
     *
     * @return the collection
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    public Collection<SourceFile> readPreviousCm()
        throws IOException, FormatException {
        // list all files in the current folder
        Collection<File> files =
            FolderUtil.listRelativeFiles(currentFolder, false);

        // add all files in the replaced folder as the replaced folder also
        // contains
        // files that were deleted in the current model
        files.addAll(FolderUtil.listRelativeFiles(replacedFolder, false));

        File addedFilesFolder = changeInformationFolder.toPath()
            .resolve(ChangeFlag.ADDITION + "/").toFile();

        // remove all files that were newly added in the current model
        files.removeAll(FolderUtil.listRelativeFiles(addedFilesFolder, false));

        // read models for the files
        Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        for (File file : files) {
            if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
                sourceFiles.add(readPreviousCmCacheFile(file));
            }
        }
        return sourceFiles;
    }

    /**
     * Read complete current code model.
     *
     * @return the collection representing the model
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    public Collection<SourceFile> readCm() throws IOException, FormatException {
        Collection<File> files =
            FolderUtil.listRelativeFiles(currentFolder, false);
        Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        for (File file : files) {
            if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
                sourceFiles.add(readCmCacheFile(file));
            }
        }
        return sourceFiles;
    }

    public Collection<SourceFile> readCm(Collection<File> paths)
        throws IOException, FormatException {
        Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        for (File file : paths) {
            SourceFile srcFile = readCm(file);
            if (srcFile != null) {
                sourceFiles.add(srcFile);
            }
        }
        return sourceFiles;
    }

    /**
     * Reads current code model for a single file within the source-tree from
     * cache.
     *
     * @param file
     *            relative file within the source-tree
     * @return the source file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    public SourceFile readCm(File file) throws IOException, FormatException {
        return readCmCacheFile(new File(getCacheFileName(file)));
    }

    /**
     * Reads previous code model for a single file within the source-tree from
     * the cache.
     *
     * @param file
     *            relative file within the source-tree
     * @return the source file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    public SourceFile readPreviousCm(File file)
        throws IOException, FormatException {
        return readPreviousCmCacheFile(new File(getCacheFileName(file)));
    }

    /**
     * Delete build model.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteBuildModel() throws IOException {
        for (Path path : BM_CACHE_FILES) {
            File fileToDelete = currentFolder.toPath().resolve(path).toFile();

            if (fileToDelete.exists()) {
                Files.move(currentFolder.toPath().resolve(path),
                    replacedFolder.toPath().resolve(path),
                    StandardCopyOption.REPLACE_EXISTING);
                flag(fileToDelete, ChangeFlag.DELETION);
            }
        }

    }

    /**
     * Gets the cache files for flag.
     *
     * @param flag
     *            the flag
     * @return the cache files for flag
     */
    public Collection<File> getCmPathsForFlag(ChangeFlag flag) {
        File flagFolder = this.changeInformationFolder.toPath()
            .resolve(flag.toString() + "/").toFile();
        Collection<File> paths = new ArrayList<File>();
        if (flagFolder.exists()) {
            for (File file : FolderUtil.listRelativeFiles(flagFolder, false)) {
                if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
                    paths.add(this.getOriginalCodeModelFile(file));
                }
            }
        }
        return paths;
    }

    /**
     * Delete variability model.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteVariabilityModel() throws IOException {
        for (Path path : VM_CACHE_FILES) {
            File fileToDelete = currentFolder.toPath().resolve(path).toFile();

            if (fileToDelete.exists()) {
                Files.move(currentFolder.toPath().resolve(path),
                    replacedFolder.toPath().resolve(path),
                    StandardCopyOption.REPLACE_EXISTING);
                flag(fileToDelete, ChangeFlag.DELETION);
            }
        }
    }

    /**
     * Rollback to previous version. This reverts all changes made for the
     * current version. Every modification is undone and the previous model will
     * get reset after it entirely replaced the current model.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void rollback() throws IOException {

        // Delete newly added files
        for (File file : getCmPathsForFlag(ChangeFlag.ADDITION)) {
            currentFolder.toPath().resolve(file.toPath()).toFile().delete();
        }
        // TODO: Check for addition in bm and vm

        // Move files that got replaced or deleted in current version
        for (File file : FolderUtil.listRelativeFiles(replacedFolder, true)) {
            Files.move(replacedFolder.toPath().resolve(file.toPath()),
                currentFolder.toPath().resolve(file.toPath()),
                StandardCopyOption.REPLACE_EXISTING);
        }

        this.clearChangeHistory();

    }

    public Collection<ChangeFlag> getVmFlags() {
        Collection<ChangeFlag> flags = new ArrayList<ChangeFlag>();
        for (ChangeFlag flag : ChangeFlag.values()) {
            boolean flagFileExists = true;
            for (Path vmCacheFile : VM_CACHE_FILES) {
                if (!this.getFlagFile(vmCacheFile.toFile(), flag).exists()) {
                    flagFileExists = false;
                    break;
                }
            }
            if (flagFileExists) {
                flags.add(flag);
            }
        }
        return flags;
    }

    public Collection<ChangeFlag> getBmFlags() {
        Collection<ChangeFlag> flags = new ArrayList<ChangeFlag>();
        for (ChangeFlag flag : ChangeFlag.values()) {
            boolean flagFileExists = true;
            for (Path vmCacheFile : BM_CACHE_FILES) {
                if (!this.getFlagFile(vmCacheFile.toFile(), flag).exists()) {
                    flagFileExists = false;
                    break;
                }
            }
            if (flagFileExists) {
                flags.add(flag);
            }
        }
        return flags;
    }

    public Collection<ChangeFlag> getFlags(@NonNull SourceFile sourceFile,
        ChangeFlag... flags) {
        Collection<ChangeFlag> changeFlags = new ArrayList<ChangeFlag>();
        File cacheFile = new File(getCacheFileName(sourceFile.getPath()));
        for (ChangeFlag flag : ChangeFlag.values()) {
            if (this.getFlagFile(cacheFile, flag).exists()) {
                changeFlags.add(flag);
            }
        }
        return changeFlags;
    }



}
