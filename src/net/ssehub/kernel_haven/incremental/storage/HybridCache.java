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
     * The folder represented by this path stores cache-files that replaced
     * files in the current model. Those files can be used to access the
     * previous model.
     */
    private static final Path REPLACED_CACHE_FOLDER =
        Paths.get("history/replaced/");

    /**
     * The folder represented by this path stores empty dummies of cache-files
     * that were added in the current model and did not replace old files. Those
     * files only carry the name of the added files but do not contain any
     * content within the file itself.
     */
    private static final Path ADDED_FOLDER = Paths.get("history/added/");

    /** Subpaths for all files representing the build-model cache. */
    private static final Path[] BM_CACHE_FILES = {Paths.get("bmCache") };

    /** Subpaths for all files representing the variability-model cache. */
    private static final Path[] VM_CACHE_FILES = {Paths.get("vmCache") };

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

    /** The current folder. */
    private File currentFolder;

    /** The replaced folder. */
    private File replacedFolder;

    /** The added folder. */
    private File addedFolder;

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
     * {@link HybridCache#replacedFolder}.
     */
    private CodeModelCache replacedCmCache;

    /**
     * Cache-Object for accessing vm-cache elements in
     * {@link HybridCache#replacedFolder}.
     */
    private VariabilityModelCache previousVmCache;

    /**
     * Cache-Object for accessing bm-cache elements in
     * {@link HybridCache#replacedFolder}.
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
            cacheFolder.toPath().resolve(REPLACED_CACHE_FOLDER).toFile();
        this.addedFolder = cacheFolder.toPath().resolve(ADDED_FOLDER).toFile();
        this.addedFolder.mkdirs();
        this.currentFolder.mkdirs();
        this.replacedFolder.mkdirs();

        this.currentBmCache = new BuildModelCache(currentFolder);
        this.currentVmCache = new VariabilityModelCache(currentFolder);
        this.currentCmCache = new CodeModelCache(currentFolder);
        this.replacedCmCache = new CodeModelCache(replacedFolder);
        this.previousVmCache = new VariabilityModelCache(replacedFolder);
        this.replacedBmCache = new BuildModelCache(replacedFolder);
    }

    /**
     * Removes all information for the previous model from cache. Only keeps
     * current model.
     */
    public void clearChangeHistory() {
        FolderUtil.deleteFolderContents(replacedFolder);
        FolderUtil.deleteFolderContents(addedFolder);
    }

    /**
     * Write a {@link SourceFile} to the cache replacing any existing model
     * accessible via {@link HybridCache#readCm()} with the same
     * {@link SourceFile#getPath()}. The previous model can thereafter be
     * accessed through {@link HybridCache#readPreviousCmCacheFile(File)}
     * 
     *
     * @param file
     *            the source file to write to the cache.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void write(SourceFile file) throws IOException {
        String fileNameInCache =
            file.getPath().toString().replace(CM_REPLACE_THIS, CM_REPLACEMENT)
                + CM_CACHE_SUFFIX;
        File fileToAdd =
            currentFolder.toPath().resolve(fileNameInCache).toFile();
        if (fileToAdd.exists()) {
            hybridDelete(new File(fileNameInCache));
        } else {
            hybridAdd(new File(fileNameInCache));
        }
        currentCmCache.write(file);
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
                hybridDelete(vmCacheFile.toFile());
            } else {
                hybridAdd(vmCacheFile.toFile());
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
                hybridDelete(bmCacheFile.toFile());
            } else {
                hybridAdd(bmCacheFile.toFile());
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
            .exists()
            && !(addedFolder.toPath().resolve(target.toPath()).toFile()
                .exists())) {
            result = currentCmCache.read(getOriginalCodeModelFile(target));
        }
        return result;

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
            result = previousVmCache.read(new File("vmCache"));
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
     * @return true, if successful
     */
    protected boolean existsInReplaced(Path[] paths) {
        boolean existsInPrevious = true;
        for (Path path : paths) {
            if (existsInPrevious) {
                existsInPrevious =
                    REPLACED_CACHE_FOLDER.resolve(path).toFile().exists();
                if (!existsInPrevious) {
                    break;
                }
            }
        }
        return existsInPrevious;
    }

    /**
     * Marks a file as added representing the changes between current and
     * previous version.
     *
     * @param target
     *            the target
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void hybridAdd(File target) throws IOException {
        File addedFile = addedFolder.toPath().resolve(target.toPath()).toFile();
        addedFile.getParentFile().mkdirs();
        addedFile.createNewFile();
    }

    /**
     * Moves a file that is deleted from the current model so that it can be
     * accessed via functions like
     * {@link HybridCache#readPreviousCmCacheFile(File)},
     * {@link HybridCache#readPreviousVm()} or
     * {@link HybridCache#readPreviousBm()}.
     *
     * @param target
     *            the target
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void hybridDelete(File target) throws IOException {
        File fileToDelete =
            currentFolder.toPath().resolve(target.toPath()).toFile();
        boolean doDelete = fileToDelete.exists();
        if (doDelete) {
            Files.move(fileToDelete.toPath(),
                replacedFolder.toPath().resolve(target.toPath()));
        }
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
        hybridDelete(new File(codeFileWithinSourceTree.getPath()
            .replace(CM_REPLACE_THIS, CM_REPLACEMENT) + CM_CACHE_SUFFIX));
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

        // remove all files that were newly added in the current model
        files.removeAll(FolderUtil.listRelativeFiles(addedFolder, false));

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
        return readCmCacheFile(
            new File(file.getPath().replace(CM_REPLACE_THIS, CM_REPLACEMENT)
                + CM_CACHE_SUFFIX));
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
        return readPreviousCmCacheFile(
            new File(file.getPath().replace(CM_REPLACE_THIS, CM_REPLACEMENT)
                + CM_CACHE_SUFFIX));
    }

    /**
     * Read the part of the codemodel that got written to the cache since the
     * last time {@link HybridCache#clearChangeHistory()} was called. This also
     * includes cached models where the models themselves did not change.
     *
     * @return the collection
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FormatException
     *             the format exception
     */
    public Collection<SourceFile> readCmNewlyWrittenParts()
        throws IOException, FormatException {
        Collection<File> files =
            FolderUtil.listRelativeFiles(replacedFolder, false);
        files.addAll(FolderUtil.listRelativeFiles(addedFolder, false));
        Collection<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        for (File file : files) {
            if (file.getPath().endsWith(CM_CACHE_SUFFIX)) {
                sourceFiles.add(readCmCacheFile(file));
            }
        }
        return sourceFiles;
    }

    /**
     * Delete build model.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteBuildModel() throws IOException {
        for (Path path : BM_CACHE_FILES) {
            hybridDelete(path.toFile());
        }

    }

    /**
     * Delete variability model.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteVariabilityModel() throws IOException {
        for (Path path : VM_CACHE_FILES) {
            hybridDelete(path.toFile());
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
        for (File file : FolderUtil.listRelativeFiles(addedFolder, true)) {
            currentFolder.toPath().resolve(file.toPath()).toFile().delete();
        }

        // Move files that got replaced or deleted in current version
        for (File file : FolderUtil.listRelativeFiles(replacedFolder, true)) {
            Files.move(replacedFolder.toPath().resolve(file.toPath()),
                currentFolder.toPath().resolve(file.toPath()),
                StandardCopyOption.REPLACE_EXISTING);
        }

        this.clearChangeHistory();

    }

}
