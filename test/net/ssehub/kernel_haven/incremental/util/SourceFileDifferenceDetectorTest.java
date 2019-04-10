package net.ssehub.kernel_haven.incremental.util;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.code_model.CodeBlock;
import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceFileChangeDetectorTest.
 */
public class SourceFileDifferenceDetectorTest extends SourceFileDifferenceDetector {

    /**
     * Creates a test variability model with the variables ALPHA, and BETA.
     * 
     * @return A test {@link VariabilityModel}.
     */
    private static VariabilityModel createTestVariabilityModel() {
        Set<VariabilityVariable> vars = new HashSet<>();
        vars.add(new VariabilityVariable("ALPHA", "bool"));
        vars.add(new VariabilityVariable("BETA", "bool"));

        VariabilityModel varModel = new VariabilityModel(new File("not_existing"), vars);
        return varModel;
    }

    /**
     * Test get original code model file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCollectRelevantElements_irrelevantCb() throws IOException {
        CodeBlock cb = new CodeBlock(12, 15, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ONLY_VARIABILITY_CHANGE, createTestVariabilityModel(), createTestVariabilityModel());
        SourceFile<CodeElement<?>> srcFile = new SourceFile<CodeElement<?>>(new File("not_existing"));
        srcFile.addElement(cb);

        Set<CodeElement<?>> collectedElements = changeDetector.collectRelevantElements(srcFile,
                new LinuxFormulaRelevancyChecker(changeDetector.varModelA, true));

        Assert.assertThat(collectedElements.size(), CoreMatchers.is(0));
    }

    /**
     * Test get original code model file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCollectRelevantElements_relevantCbs() throws IOException {
        CodeBlock cb = new CodeBlock(12, 15, new File("file"), not("ALPHA"), not("BETA"));
        CodeBlock cb2 = new CodeBlock(17, 20, new File("file"), not("CONFIG_Test"), not("CONFIG_Tests"));
        CodeBlock cb3 = new CodeBlock(22, 30, new File("file"), not("Test_MODULE"), not("Test_MODULE"));
        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ONLY_VARIABILITY_CHANGE, createTestVariabilityModel(), createTestVariabilityModel());
        SourceFile<CodeElement<?>> srcFile = new SourceFile<CodeElement<?>>(new File("not_existing"));
        srcFile.addElement(cb);
        srcFile.addElement(cb2);
        srcFile.addElement(cb3);

        Set<CodeElement<?>> collectedElements = changeDetector.collectRelevantElements(srcFile,
                new LinuxFormulaRelevancyChecker(changeDetector.varModelA, true));

        Assert.assertThat(collectedElements, CoreMatchers.hasItems(cb, cb2, cb3));
    }

    /**
     * Test get original code model file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCollectRelevantElements_nestedCbs() throws IOException {
        // cb and cb_child are relevant because cb is already directly relevant
        CodeBlock cb = new CodeBlock(12, 15, new File("file"), not("CONFIG_Test"), not("CONFIG_Tests"));
        CodeBlock cbChild = new CodeBlock(13, 14, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        cb.addNestedElement(cbChild);

        // both blocks are irrelevant
        CodeBlock cb2 = new CodeBlock(17, 20, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        CodeBlock cb2Child = new CodeBlock(18, 19, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        cb2.addNestedElement(cb2Child);

        // cb3_child is the only element within cb3 that is directly relvevant
        // however because cb3_child is relevant, cb3 and cb3_child_child are indirectly
        // relevant.
        // that leaves us with cb3_child2 being the only irrelevant block within cb3
        CodeBlock cb3 = new CodeBlock(22, 30, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        CodeBlock cb3Child = new CodeBlock(23, 26, new File("file"), not("CONFIG_Test"), not("CONFIG_Test"));
        CodeBlock cb3ChildChild =
                new CodeBlock(24, 25, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        CodeBlock cb3Child2 = new CodeBlock(28, 29, new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        cb3.addNestedElement(cb3Child);
        cb3.addNestedElement(cb3Child2);
        cb3Child.addNestedElement(cb3ChildChild);

        SourceFile<CodeElement<?>> srcFile = new SourceFile<CodeElement<?>>(new File("not_existing"));
        srcFile.addElement(cb);
        srcFile.addElement(cb2);
        srcFile.addElement(cb3);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ONLY_VARIABILITY_CHANGE, createTestVariabilityModel(), createTestVariabilityModel());

        Set<CodeElement<?>> collectedElements = changeDetector.collectRelevantElements(srcFile,
                new LinuxFormulaRelevancyChecker(changeDetector.varModelA, true));

        Assert.assertThat(collectedElements, CoreMatchers.hasItems(cb, cbChild, cb3, cb3ChildChild, cb3Child));
        Assert.assertThat(collectedElements, CoreMatchers.not(CoreMatchers.hasItem(cb2)));
        Assert.assertThat(collectedElements, CoreMatchers.not(CoreMatchers.hasItem(cb2Child)));
        Assert.assertThat(collectedElements, CoreMatchers.not(CoreMatchers.hasItem(cb3Child2)));
        Assert.assertThat(collectedElements.size(), CoreMatchers.is(5));
    }

    /**
     * Checks if is structure same any change unchanged.
     */
    @Test
    public void isDifferent_anyChange_unchanged() {
        SourceFile<CodeElement<?>> fileA = generateSrcFile(0, false, false);
        SourceFile<CodeElement<?>> fileB = generateSrcFile(0, false, false);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(Consideration.ANY_CHANGE,
                createTestVariabilityModel(), createTestVariabilityModel());

        Assert.assertThat(changeDetector.isDifferent(fileA, fileB), CoreMatchers.is(Boolean.FALSE));
    }

    /**
     * Checks if is structure same any change changed.
     */
    @Test
    public void isDifferent_anyChange_changed() {
        SourceFile<CodeElement<?>> fileA = generateSrcFile(1, false, false);
        SourceFile<CodeElement<?>> fileB = generateSrcFile(2, false, false);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(Consideration.ANY_CHANGE,
                createTestVariabilityModel(), createTestVariabilityModel());

        Assert.assertThat(changeDetector.isDifferent(fileA, fileB), CoreMatchers.is(Boolean.TRUE));

    }

    /**
     * Checks if is structure same any change except line change unchanged.
     */
    @Test
    public void isDifferent_anyChangeExceptLineChange_unchanged() {
        SourceFile<CodeElement<?>> fileA = generateSrcFile(1, false, false);
        SourceFile<CodeElement<?>> fileB = generateSrcFile(0, false, false);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ANY_CHANGE_EXCEPT_LINECHANGE, createTestVariabilityModel(), createTestVariabilityModel());

        Assert.assertThat(changeDetector.isDifferent(fileA, fileB), CoreMatchers.is(Boolean.FALSE));
    }

    /**
     * Checks if is structure same any change except line change changed.
     */
    @Test
    public void isDifferent_anyChangeExceptLineChange_changed() {
        SourceFile<CodeElement<?>> fileA = generateSrcFile(1, true, false);
        SourceFile<CodeElement<?>> fileB = generateSrcFile(0, false, false);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ANY_CHANGE_EXCEPT_LINECHANGE, createTestVariabilityModel(), createTestVariabilityModel());

        Assert.assertThat(changeDetector.isDifferent(fileA, fileB), CoreMatchers.is(Boolean.TRUE));
    }

    /**
     * Checks if is structure same only variability change unchanged.
     */
    @Test
    public void isDifferent_onlyVariabilityChange_unchanged() {
        SourceFile<CodeElement<?>> fileA = generateSrcFile(1, true, false);
        SourceFile<CodeElement<?>> fileB = generateSrcFile(2, false, false);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ONLY_VARIABILITY_CHANGE, createTestVariabilityModel(), createTestVariabilityModel());

        Assert.assertThat(changeDetector.isDifferent(fileA, fileB), CoreMatchers.is(Boolean.FALSE));

    }

    /**
     * Checks if is structure same only variability change changed.
     */
    @Test
    public void isDifferent_onlyVariabilityChange_changed() {
        SourceFile<CodeElement<?>> fileA = generateSrcFile(1, true, true);
        SourceFile<CodeElement<?>> fileB = generateSrcFile(2, false, false);

        SourceFileDifferenceDetector changeDetector = new SourceFileDifferenceDetector(
                Consideration.ONLY_VARIABILITY_CHANGE, createTestVariabilityModel(), createTestVariabilityModel());

        Assert.assertThat(changeDetector.isDifferent(fileA, fileB), CoreMatchers.is(Boolean.TRUE));

    }

    /**
     * Generate src file.
     *
     * @param lineNumberModificator the line number modificator
     * @param modifyIrrelevantBlock the modify irrelevant block
     * @param modifyRelevantBlock   the modify relevant block
     * @return the source file
     */
    public SourceFile<CodeElement<?>> generateSrcFile(int lineNumberModificator, boolean modifyIrrelevantBlock,
            boolean modifyRelevantBlock) {
        // cb and cb_child are relevant because cb is already directly relevant
        CodeBlock cb = new CodeBlock(12 + lineNumberModificator, 15 + lineNumberModificator, new File("file"),
                not("CONFIG_Test"), not("CONFIG_Tests"));
        CodeBlock cbChild = new CodeBlock(13 + lineNumberModificator, 14 + lineNumberModificator, new File("file"),
                not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        cb.addNestedElement(cbChild);

        // cb3_child is the only element within cb3 that is directly relvevant
        // however because cb3_child is relevant, cb3 and cb3_child_child are indirectly
        // relevant.
        // that leaves us with cb3_child2 being the only irrelevant block within cb3
        CodeBlock cb3 = new CodeBlock(22 + lineNumberModificator, 30 + lineNumberModificator, new File("file"),
                not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        CodeBlock cb3Child = new CodeBlock(23 + lineNumberModificator, 26 + lineNumberModificator, new File("file"),
                not("CONFIG_Test"), not("CONFIG_Test"));
        CodeBlock cb3ChildChild = new CodeBlock(24 + lineNumberModificator, 25 + lineNumberModificator,
                new File("file"), not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        CodeBlock cb3Child2 = new CodeBlock(28 + lineNumberModificator, 29 + lineNumberModificator, new File("file"),
                not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
        cb3.addNestedElement(cb3Child);
        if (!modifyIrrelevantBlock) {
            cb3.addNestedElement(cb3Child2);
        }
        if (!modifyRelevantBlock) {
            cb3Child.addNestedElement(cb3ChildChild);
        }

        SourceFile<CodeElement<?>> srcFile = new SourceFile<CodeElement<?>>(new File("not_existing"));
        srcFile.addElement(cb);

        if (!modifyIrrelevantBlock) {
            // both blocks are irrelevant
            CodeBlock cb2 = new CodeBlock(17 + lineNumberModificator, 20 + lineNumberModificator, new File("file"),
                    not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
            CodeBlock cb2Child = new CodeBlock(18 + lineNumberModificator, 19 + lineNumberModificator, new File("file"),
                    not("NOT_IN_VAR_MODEL"), not("NOT_IN_VAR_MODEL"));
            cb2.addNestedElement(cb2Child);
            srcFile.addElement(cb2);
        }
        srcFile.addElement(cb3);
        return srcFile;
    }

}
