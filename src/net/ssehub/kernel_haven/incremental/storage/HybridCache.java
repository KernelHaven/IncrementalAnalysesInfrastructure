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
import net.ssehub.kernel_haven.build_model.JsonBuildModelCache;
import net.ssehub.kernel_haven.code_model.JsonCodeModelCache;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.JsonVariabilityModelCache;

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
    private static final String CM_CACHE_SUFFIX = ".json";

    /**
     * The folder represented by this path stores cache-files that replaced files in
     * the current model. Those files can be used to access the previous model.
     */
    private static final Path REPLACED_FOLDER = Paths.get("history/backup/");

    /** The Constant FLAG_FOLDER. */
    private static final Path CHANGE_INFORMATION_FOLDER = Paths.get("history/change-information/");

    /**
     * The Enum ChangeFlag.
     */
    public enum ChangeFlag {

        /**
         * Auxilarry change that was not made through extraction but by other means. An
         * example for this is when only the linenumbers of the extracted model are
         * adjusted.
         */
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
         * @param flag the flag
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

    /** relative path to build model cache file within cache directory. */
    private static final Path BM_CACHE_FILE = Paths.get("bmCache.json");

    /** relative path to variability model cache file within cache directory. */
    private static final Path VM_CACHE_FILE = Paths.get("vmCache.json");

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
    private JsonCodeModelCache currentCmCache;

    /**
     * Cache-Object for accessing vm-cache elements in
     * {@link HybridCache#currentFolder}.
     */
    private JsonVariabilityModelCache currentVmCache;

    /**
     * Cache-Object for accessing bm-cache elements in
     * {@link HybridCache#currentFolder}.
     */
    private JsonBuildModelCache currentBmCache;

    /**
     * Cache-Object for accessing cm-cache elements in {@link HybridCache#backup}.
     */
    private JsonCodeModelCache replacedCmCache;

    /**
     * Cache-Object for accessing vm-cache elements in {@link HybridCache#backup}.
     */
    private JsonVariabilityModelCache replacedVmCache;

    /**
     * Cache-Object for accessing bm-cache elements in {@link HybridCache#backup}.
     */
    private JsonBuildModelCache replacedBmCache;

    /**
     * Instantiates a new hybrid cache.
     */
    protected HybridCache() {
        // Empty constructor for JUnit-Tests only
    }

    /**
     * Instantiates a new hybrid cache.
     *
     * @param cacheFolder the cache folder
     */
    public HybridCache(File cacheFolder) {
        this.currentFolder = cacheFolder.toPath().resolve(CURRENT_CACHE_FOLDER).toFile();
        this.replacedFolder = cacheFolder.toPath().resolve(REPLACED_FOLDER).toFile();
        this.changeInformationFolder = cacheFolder.toPath().resolve(CHANGE_INFORMATION_FOLDER).toFile();
        this.currentFolder.mkdirs();
        this.replacedFolder.mkdirs();
        this.changeInformationFolder.mkdir();
        this.currentBmCache = new JsonBuildModelCache(currentFolder);
        this.currentVmCache = new JsonVariabilityModelCache(currentFolder);
        this.currentCmCache = new JsonCodeModelCache(currentFolder);
        this.replacedCmCache = new JsonCodeModelCache(replacedFolder);
        this.replacedVmCache = new JsonVariabilityModelCache(replacedFolder);
        this.replacedBmCache = new JsonBuildModelCache(replacedFolder);
    }

    /**
     * Gets the cache file name.
     *
     * @param file the file
     * @return the cache file name
     */
    public static String getCacheFileName(File file) {
        return file.getPath().replace(CM_REPLACE_THIS, CM_REPLACEMENT) + CM_CACHE_SUFFIX;
    }

    /**
     * Removes all information for the previous model from cache. Only keeps current
     * model.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void clearChangeHistory() throws IOException {
        FolderUtil.deleteFolderContents(replacedFolder);
        FolderUtil.deleteFolderContents(changeInformationFolder);
    }

    /**
     * Write a {@link SourceFile} to the cache replacing any existing model
     * accessible via {@link HybridCache#readCm()} with the same
     * {@link SourceFile#getPath()}. The previous model can thereafter be accessed
     * through {@link HybridCache#readPreviousCmCacheFile(File)}
     *
     * @param sourceFile the source file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(SourceFile<?> sourceFile) throws IOException {
        String fileNameInCache = getCacheFileName(sourceFile.getPath());
        File newFile = currentFolder.toPath().resolve(fileNameInCache).toFile();
        if (newFile.exists()) {
            Files.move(newFile.toPath(), replacedFolder.toPath().resolve(fileNameInCache),
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
     * @param file the file
     * @param flag the flag
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void flag(SourceFile<?> file, ChangeFlag flag) throws IOException {
        String fileNameInCache = getCacheFileName(file.getPath());
        flag(new File(fileNameInCache), flag);
    }

    /**
     * Adds a flag to the SourceFile within the HybridCache. This flag is part of
     * the file-history and should describe changes made to the file in the current
     * iteration. It is possible to assign multiple flags to a file.
     *
     * @param cacheFile the cache file
     * @param flag      the flag
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void flag(File cacheFile, ChangeFlag flag) throws IOException {
        File flagFile = this.getFlagFile(cacheFile, flag);
        flagFile.getParentFile().mkdirs();
        if (!flagFile.exists()) {
            flagFile.createNewFile();
        }
    }

    /**
     * Gets the flag file.
     *
     * @param cacheFile the cache file
     * @param flag      the flag
     * @return the flag file
     */
    private File getFlagFile(File cacheFile, ChangeFlag flag) {
        String fileNameInCache = flag.toString() + "/" + cacheFile.getPath();
        return changeInformationFolder.toPath().resolve(fileNameInCache).toFile();
    }

    /**
     * Write the {@link VariabilityModel} to the cache replacing the model
     * accessible via {@link HybridCache#readVm()}. The previous model can
     * thereafter be accessed through {@link HybridCache#readPreviousVm()}
     *
     * @param vmModel the vm model to write to the cache.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(VariabilityModel vmModel) throws IOException {

        File fileToAdd = currentFolder.toPath().resolve(VM_CACHE_FILE).toFile();
        if (fileToAdd.exists()) {
            flag(VM_CACHE_FILE.toFile(), ChangeFlag.MODIFICATION);
            Files.move(currentFolder.toPath().resolve(VM_CACHE_FILE), replacedFolder.toPath().resolve(VM_CACHE_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
        } else {
            flag(VM_CACHE_FILE.toFile(), ChangeFlag.ADDITION);
        }

        currentVmCache.write(vmModel);
    }

    /**
     * Flag build model.
     *
     * @param flag the flag
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void flagBuildModel(ChangeFlag flag) throws IOException {

        flag(BM_CACHE_FILE.toFile(), flag);

    }

    /**
     * Flag variability model.
     *
     * @param flag the flag
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void flagVariabilityModel(ChangeFlag flag) throws IOException {

        flag(VM_CACHE_FILE.toFile(), flag);

    }

    /**
     * Write the {@link BuildModel} to the cache replacing the model accessible via
     * {@link HybridCache#readBm()}. The previous model can thereafter be accessed
     * through {@link HybridCache#readPreviousBm()}
     *
     * @param buildModel the build model
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void write(BuildModel buildModel) throws IOException {
        File fileToAdd = currentFolder.toPath().resolve(BM_CACHE_FILE).toFile();
        if (fileToAdd.exists()) {
            flag(BM_CACHE_FILE.toFile(), ChangeFlag.MODIFICATION);
            Files.move(currentFolder.toPath().resolve(BM_CACHE_FILE), replacedFolder.toPath().resolve(BM_CACHE_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
        } else {
            flag(BM_CACHE_FILE.toFile(), ChangeFlag.ADDITION);
        }

        currentBmCache.write(buildModel);
    }

    /**
     * Read cm from a cache file in current version. In contrast to
     * {@link CodeModelCache#read(File)} this expects the name of the cache file
     * instead of the name of the file in the source-tree.
     *
     * @param cacheFile the target
     * @return the source file
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    protected SourceFile<?> readCmCacheFile(File cacheFile) throws IOException, FormatException {
        File originalFile = getOriginalCodeModelFile(cacheFile);

        SourceFile<?> srcFile = null;
        if (originalFile != null) {
            srcFile = currentCmCache.read(originalFile);
        }
        return srcFile;
    }

    /**
     * Gets the original file object (File within the source-tree) corresponding to
     * the cached file.
     *
     * @param cachedFile the cached file
     * @return the original file
     */
    protected File getOriginalCodeModelFile(File cachedFile) {
        String cachedFilePath = cachedFile.getPath();
        String originalFilePath = null;
        File originalFile = null;

        Matcher matcher = Pattern.compile("^([\\S]+)(\\.[^\\.]+)(" + CM_CACHE_SUFFIX.replace(".", "\\.") + ")$")
                .matcher(cachedFilePath);

        if (matcher.find() && matcher.groupCount() >= 2) {
            originalFilePath = matcher.group(1).replace('.', '/') + matcher.group(2);
            originalFile = new File(originalFilePath);

        }

        return originalFile;
    }

    /**
     * Read build model in current version.
     *
     * @return the builds the model
     * @throws FormatException the format exception
     * @throws IOException     Signals that an I/O exception has occurred.
     */
    public BuildModel readBm() throws FormatException, IOException {
        return currentBmCache.read(BM_CACHE_FILE.toFile());

    }

    /**
     * Read variability model in current version.
     *
     * @return the variability model
     * @throws FormatException the format exception
     * @throws IOException     Signals that an I/O exception has occurred.
     */
    public VariabilityModel readVm() throws FormatException, IOException {
        return currentVmCache.read(VM_CACHE_FILE.toFile());

    }

    /**
     * Read code model in previous version from a cache-file.
     * 
     * @param target File object representing a cache-file.
     * @return the source file
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    protected SourceFile<?> readPreviousCmCacheFile(File target) throws IOException, FormatException {
        SourceFile<?> result = null;
        // read from replaced folder if file was deleted or got replaced through
        // the
        // current version
        if (replacedFolder.toPath().resolve(target.toPath()).toFile().exists()) {
            result = replacedCmCache.read(getOriginalCodeModelFile(target));

            /*
             * read from current folder if file was not newly added as the file was not
             * touched and remains the same in both the current and previous version
             */
        } else if (currentFolder.toPath().resolve(target.toPath()).toFile().exists()
                && !(cacheFileHasFlag(target, ChangeFlag.ADDITION))) {
            result = currentCmCache.read(getOriginalCodeModelFile(target));
        }
        return result;

    }

    /**
     * Cache file has flag.
     *
     * @param target the target
     * @param flag   the flag
     * @return true, if successful
     */
    private boolean cacheFileHasFlag(File target, ChangeFlag flag) {
        return changeInformationFolder.toPath().resolve(flag + "/" + target.getPath()).toFile().exists();
    }

    /**
     * Read build model in previous version.
     *
     * @return the builds the model
     * @throws FormatException the format exception
     * @throws IOException     Signals that an I/O exception has occurred.
     */
    public BuildModel readPreviousBm() throws FormatException, IOException {
        boolean previousModelExists = existsInReplaced(BM_CACHE_FILE);

        BuildModel result = null;
        if (previousModelExists) {
            result = replacedBmCache.read(BM_CACHE_FILE.toFile());
        } else {
            result = currentBmCache.read(BM_CACHE_FILE.toFile());
        }
        return result;

    }

    /**
     * Read variability model in previous version.
     *
     * @return the variability model
     * @throws FormatException the format exception
     * @throws IOException     Signals that an I/O exception has occurred.
     */
    public VariabilityModel readPreviousVm() throws FormatException, IOException {
        boolean previousModelExists = existsInReplaced(VM_CACHE_FILE);

        VariabilityModel result = null;
        if (previousModelExists) {
            result = replacedVmCache.read(VM_CACHE_FILE.toFile());
        } else {
            result = currentVmCache.read(VM_CACHE_FILE.toFile());
        }

        return result;
    }

    /**
     * Checks if the paths exist as subpaths within the replaced-folder.
     *
     * @param path the path
     * @return true, if exists
     */
    protected boolean existsInReplaced(Path path) {
        return REPLACED_FOLDER.resolve(path).toFile().exists();
    }

    /**
     * Delete code model for a code-file within the source-tree.
     *
     * @param codeFileWithinSourceTree the path
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void deleteCodeModel(File codeFileWithinSourceTree) throws IOException {
        File fileToDelete = currentFolder.toPath().resolve(getCacheFileName(codeFileWithinSourceTree)).toFile();
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    /**
     * Read complete previous code model.
     *
     * @return the collection
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    public Collection<SourceFile<?>> readPreviousCm() throws IOException, FormatException {
        // list all files in the current folder
        Collection<File> files = FolderUtil.listRelativeFiles(currentFolder, false);

        // add all files in the replaced folder as the replaced folder also
        // contains
        // files that were deleted in the current model
        files.addAll(FolderUtil.listRelativeFiles(replacedFolder, false));

        File addedFilesFolder = changeInformationFolder.toPath().resolve(ChangeFlag.ADDITION + "/").toFile();

        // remove all files that were newly added in the current model
        files.removeAll(FolderUtil.listRelativeFiles(addedFilesFolder, false));

        // read models for the files
        Collection<SourceFile<?>> sourceFiles = new ArrayList<SourceFile<?>>();
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
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    public Collection<SourceFile<?>> readCm() throws IOException, FormatException {
        Collection<File> files = FolderUtil.listRelativeFiles(currentFolder, false);
        Collection<SourceFile<?>> sourceFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
                sourceFiles.add(readCmCacheFile(file));
            }
        }
        return sourceFiles;
    }

    /**
     * Read cm.
     *
     * @param paths the paths
     * @return the collection
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    public Collection<SourceFile<?>> readCm(Collection<File> paths) throws IOException, FormatException {
        Collection<SourceFile<?>> sourceFiles = new ArrayList<>();
        for (File file : paths) {
            SourceFile<?> srcFile = readCm(file);
            if (srcFile != null) {
                sourceFiles.add(srcFile);
            }
        }
        return sourceFiles;
    }

    /**
     * Reads current code model for a single file within the source-tree from cache.
     *
     * @param file relative file within the source-tree
     * @return the source file
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    public SourceFile<?> readCm(File file) throws IOException, FormatException {
        return readCmCacheFile(new File(getCacheFileName(file)));
    }

    /**
     * Reads previous code model for a single file within the source-tree from the
     * cache.
     *
     * @param file relative file within the source-tree
     * @return the source file
     * @throws IOException     Signals that an I/O exception has occurred.
     * @throws FormatException the format exception
     */
    public SourceFile<?> readPreviousCm(File file) throws IOException, FormatException {
        return readPreviousCmCacheFile(new File(getCacheFileName(file)));
    }

    /**
     * Delete build model.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void deleteBuildModel() throws IOException {

        File fileToDelete = currentFolder.toPath().resolve(BM_CACHE_FILE).toFile();

        if (fileToDelete.exists()) {
            Files.move(currentFolder.toPath().resolve(BM_CACHE_FILE), replacedFolder.toPath().resolve(BM_CACHE_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
            flag(fileToDelete, ChangeFlag.DELETION);
        }

    }

    /**
     * Gets the cache files for flag.
     *
     * @param flag the flag
     * @return the cache files for flag
     */
    public Collection<File> getCmPathsForFlag(ChangeFlag flag) {
        File flagFolder = this.changeInformationFolder.toPath().resolve(flag.toString() + "/").toFile();
        Collection<File> paths = new ArrayList<>();
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void deleteVariabilityModel() throws IOException {
        File fileToDelete = currentFolder.toPath().resolve(VM_CACHE_FILE).toFile();
        if (fileToDelete.exists()) {
            Files.move(currentFolder.toPath().resolve(VM_CACHE_FILE), replacedFolder.toPath().resolve(VM_CACHE_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
            flag(fileToDelete, ChangeFlag.DELETION);
        }

    }

    /**
     * Rollback to previous version. This reverts all changes made for the current
     * version. Every modification is undone and the previous model will get reset
     * after it entirely replaced the current model.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void rollback() throws IOException {

        // Delete newly added files
        for (File file : getCmPathsForFlag(ChangeFlag.ADDITION)) {
            currentFolder.toPath().resolve(file.toPath()).toFile().delete();
        }

        // Move files that got replaced or deleted in current version
        for (File file : FolderUtil.listRelativeFiles(replacedFolder, true)) {
            Files.move(replacedFolder.toPath().resolve(file.toPath()), currentFolder.toPath().resolve(file.toPath()),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        this.clearChangeHistory();

    }

    /**
     * Gets the vm flags.
     *
     * @return the vm flags
     */
    public Collection<ChangeFlag> getVmFlags() {
        Collection<ChangeFlag> flags = new ArrayList<>();
        for (ChangeFlag flag : ChangeFlag.values()) {
            if (this.getFlagFile(VM_CACHE_FILE.toFile(), flag).exists()) {
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * Gets the bm flags.
     *
     * @return the bm flags
     */
    public Collection<ChangeFlag> getBmFlags() {
        Collection<ChangeFlag> flags = new ArrayList<>();
        for (ChangeFlag flag : ChangeFlag.values()) {
            if (this.getFlagFile(BM_CACHE_FILE.toFile(), flag).exists()) {
                flags.add(flag);
            }
        }
        return flags;
    }

    /**
     * Gets the flags.
     *
     * @param sourceFile the source file
     * @return the flags
     */
    public Collection<ChangeFlag> getFlags(@NonNull SourceFile<?> sourceFile) {
        Collection<ChangeFlag> changeFlags = new ArrayList<>();
        File cacheFile = new File(getCacheFileName(sourceFile.getPath()));
        for (ChangeFlag flag : ChangeFlag.values()) {
            if (this.getFlagFile(cacheFile, flag).exists()) {
                changeFlags.add(flag);
            }
        }
        return changeFlags;
    }

}
