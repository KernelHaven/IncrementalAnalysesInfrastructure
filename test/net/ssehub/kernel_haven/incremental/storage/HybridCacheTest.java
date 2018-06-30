package net.ssehub.kernel_haven.incremental.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.CodeBlock;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.incremental.util.FolderUtil;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.Variable;

/**
 * 
 * Tests for the {@link HybridCache}.
 * 
 * @author moritz
 */
public class HybridCacheTest extends HybridCache {

    /** The Constant TESTFOLDER_HYBRID_DELETE. */
    private static final File TESTFOLDER_HYBRID_DELETE =
        new File("testdata/hybrid-cache/hybrid-delete");

    /** The logger. */
    private static Logger LOGGER;

    /**
     * Inits the logger.
     */
    @BeforeClass
    public static void initLogger() {
        LOGGER = Logger.get();
        LOGGER.setLevel(Level.DEBUG);

    }

    /**
     * Test hybrid add.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testHybridAdd() throws IOException {
        Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

        HybridCache cache = new HybridCache(tempFolder.toFile());

        cache.hybridAdd(new File("test.txt"));

        Assert.assertThat(
            FolderUtil.listRelativeFiles(tempFolder.toFile(), true),
            CoreMatchers.hasItem(new File("history/added/test.txt")));
    }

    /**
     * Test hybrid delete.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testHybridDelete() throws IOException {
        Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

        // Setup
        FolderUtil.copyFolderContent(TESTFOLDER_HYBRID_DELETE,
            tempFolder.toFile());
        HybridCache cache = new HybridCache(tempFolder.toFile());

        cache.hybridDelete(new File("test.file"));

        Assert.assertThat(
            FolderUtil.listRelativeFiles(tempFolder.toFile(), true),
            CoreMatchers.hasItem(new File("history/replaced/test.file")));
    }

    /**
     * Test get original code model file.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetOriginalCodeModelFile() throws IOException {
        Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

        HybridCache cache = new HybridCache(tempFolder.toFile());

        Assert.assertThat(
            cache.getOriginalCodeModelFile(new File("cached.file.c.cache")),
            CoreMatchers.equalTo(new File("cached/file.c")));

        Assert.assertThat(
            cache.getOriginalCodeModelFile(new File("dir.cached.file.c.cache")),
            CoreMatchers.equalTo(new File("dir/cached/file.c")));

    }

    // CHECKSTYLE:OFF
    /**
     * Test write source file added file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testWriteSourceFile_addedFile() throws Exception {

        // Set up HybridCache
        Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

        HybridCache cache = new HybridCache(tempFolder.toFile());

        // Create a source file object
        File location = new File("test.c");
        SourceFile originalSourceFile = new SourceFile(location);
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        CodeBlock block1 = new CodeBlock(1, 2, new File("file"), a, a);
        CodeBlock block2 = new CodeBlock(3, 15, new File("file"),
            new Negation(a), new Negation(a));
        CodeBlock block21 = new CodeBlock(4, 5, new File("file"), b,
            new Conjunction(b, new Negation(a)));
        block2.addNestedElement(block21);

        originalSourceFile.addElement(block1);
        originalSourceFile.addElement(block2);

        // write source file object to cache
        cache.write(originalSourceFile);

        // check if file is correctly represented in cache
        Assert.assertThat(
            FolderUtil.listRelativeFiles(tempFolder.toFile(), true),
            CoreMatchers.hasItems(new File("current/test.c.cache"),
                new File("history/added/test.c.cache")));

    }

    /**
     * Test write source file replaced file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testWriteSourceFile_replacedFile() throws Exception {

        // Set up HybridCache
        Path tempFolder = Files.createTempDirectory("hybrid-cache-test");

        HybridCache cache = new HybridCache(tempFolder.toFile());

        // Create a source file object
        File location = new File("test.c");
        SourceFile originalSourceFile = new SourceFile(location);
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        CodeBlock block1 = new CodeBlock(1, 2, new File("file"), a, a);
        CodeBlock block2 = new CodeBlock(3, 15, new File("file"),
            new Negation(a), new Negation(a));
        CodeBlock block21 = new CodeBlock(4, 5, new File("file"), b,
            new Conjunction(b, new Negation(a)));
        block2.addNestedElement(block21);

        originalSourceFile.addElement(block1);
        originalSourceFile.addElement(block2);

        /*
         * Create empty code model file in cache that will be replaced when
         * writing to cache
         */
        File existingCodeModelCacheFile =
            tempFolder.resolve("current/test.c.cache").toFile();
        existingCodeModelCacheFile.getParentFile().mkdirs();
        existingCodeModelCacheFile.createNewFile();

        // overwrite object in cache
        cache.write(originalSourceFile);

        // check if file is correctly represented in cache
        Assert.assertThat(
            FolderUtil.listRelativeFiles(tempFolder.toFile(), true),
            CoreMatchers.hasItems(new File("current/test.c.cache"),
                new File("history/replaced/test.c.cache")));
        Assert.assertThat(
            FolderUtil.listRelativeFiles(tempFolder.toFile(), true).size(),
            CoreMatchers.equalTo(2));

    }
    // CHECKSTYLE:ON

}
