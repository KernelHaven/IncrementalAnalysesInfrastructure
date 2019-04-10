package net.ssehub.kernel_haven.incremental.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.ssehub.kernel_haven.code_model.CodeElement;
import net.ssehub.kernel_haven.code_model.SourceFile;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;

// TODO: Auto-generated Javadoc
/**
 * The Class CodeFileComparator.
 * 
 * @author Moritz
 */
public class SourceFileDifferenceDetector {

    /**
     * The Enum Consideration.
     */
    public static enum Consideration {

        /** The all elements. */
        ANY_CHANGE,
        /** The ignore linechange. */
        ANY_CHANGE_EXCEPT_LINECHANGE,
        /** The only variability change. */
        ONLY_VARIABILITY_CHANGE;
    }

    /** The var model A. */
    protected VariabilityModel varModelA;

    /** The var model B. */
    protected VariabilityModel varModelB;

    /** The consideration. */
    private Consideration consideration;

    /**
     * Instantiates a new code file comparator.
     *
     * @param consideration the consideration
     * @param varModelA     the var model A
     * @param varModelB     the var model B
     */
    public SourceFileDifferenceDetector(Consideration consideration, VariabilityModel varModelA,
            VariabilityModel varModelB) {
        this.consideration = consideration;
        this.varModelA = varModelA;
        this.varModelB = varModelB;
    }

    /**
     * Instantiates a new source file change detector.
     */
    protected SourceFileDifferenceDetector() {

    }

    /**
     * Checks for changed.
     *
     * @param fileA the file A
     * @param fileB the file B
     * @return true, if changed
     */
    public boolean isDifferent(SourceFile<?> fileA, SourceFile<?> fileB) {
        boolean different = false;
        if (!fileA.equals(fileB)) {
            if (this.consideration == Consideration.ANY_CHANGE) {
                different = true;
            } else {

                if (this.consideration == Consideration.ONLY_VARIABILITY_CHANGE) {
                    Set<CodeElement<?>> relevancyA =
                            collectRelevantElements(fileA, new LinuxFormulaRelevancyChecker(varModelA, true));
                    Set<CodeElement<?>> relevancyB =
                            collectRelevantElements(fileB, new LinuxFormulaRelevancyChecker(varModelB, true));
                    different = !isStructureSame(fileA, fileB, relevancyA, relevancyB);
                } else {
                    different = !isStructureSame(fileA, fileB, null, null);
                }
            }

        }
        return different;
    }

    /**
     * Collects {@link CodeElement}s that are considered relevant from a given
     * {@link SourceFile}. If {@link FormulaRelevancyChecker} considers an element
     * to be relevant, all of its parents as well as children will be considered
     * relevant as well.
     *
     * @param file    the file
     * @param checker the checker
     * @return the sets the
     */
    protected Set<CodeElement<?>> collectRelevantElements(SourceFile<?> file, LinuxFormulaRelevancyChecker checker) {
        Set<CodeElement<?>> relevantElements = new HashSet<CodeElement<?>>();
        for (CodeElement<?> element : file) {
            collectRelevantElements(element, checker, new HashSet<CodeElement<?>>(), new HashSet<CodeElement<?>>(),
                    relevantElements);
        }
        return relevantElements;
    }

    /**
     * Collects {@link CodeElement}s that are considered relevant from a given
     * {@link CodeElement}. If {@link FormulaRelevancyChecker} considers an element
     * to be relevant, all of its parents as well as children will be considered
     * relevant as well.
     *
     * @param currentElement           the current element
     * @param checker                  the checker
     * @param parents                  the parents
     * @param directlyRelevantElements the directly relevant elements
     * @param relevantElements         the relevant elements
     */
    protected void collectRelevantElements(CodeElement<?> currentElement, LinuxFormulaRelevancyChecker checker,
            Set<CodeElement<?>> parents, Set<CodeElement<?>> directlyRelevantElements,
            Set<CodeElement<?>> relevantElements) {
        // if the path contains an element that is directly relevant,
        // currentElement is relevant as well.
        if (!Collections.disjoint(directlyRelevantElements, parents)) {
            relevantElements.add(currentElement);
            // Otherwise check if the element is relevant on its own (=directly relevant)
        } else if (checker.visit(currentElement.getPresenceCondition())) {
            relevantElements.add(currentElement);
            directlyRelevantElements.add(currentElement);
            // if the element is element on its own, it also makes all of its parents
            // relevant
            relevantElements.addAll(parents);
        }

        // After the current element itself was handled, take care of its nested
        // elements
        int nestedCount = currentElement.getNestedElementCount();
        if (nestedCount > 0) {
            // Create a copy of the parent list and add currentElement as a parent
            Set<CodeElement<?>> newParents = new HashSet<CodeElement<?>>(parents);
            newParents.add(currentElement);
            for (int i = 0; i < nestedCount; i++) {
                CodeElement<?> nestedElement = currentElement.getNestedElement(i);
                collectRelevantElements(nestedElement, checker, newParents, directlyRelevantElements, relevantElements);
            }
        }

    }

    /**
     * Checks for changes within the structure of two given source files.
     *
     * @param fileA                   the file A
     * @param fileB                   the file B
     * @param relevantElementsInFileA the relevancy A
     * @param relevantElementsInFileB the relevancy B
     * @return true, if unchanged
     */
    protected boolean isStructureSame(SourceFile<?> fileA, SourceFile<?> fileB,
            Set<CodeElement<?>> relevantElementsInFileA, Set<CodeElement<?>> relevantElementsInFileB) {

        List<CodeElement<?>> fileAelements =
                getListOfRelevantNestedElements((Iterable<CodeElement<?>>) fileA, relevantElementsInFileA);
        List<CodeElement<?>> fileBelements =
                getListOfRelevantNestedElements((Iterable<CodeElement<?>>) fileB, relevantElementsInFileB);

        boolean same = fileAelements.size() == fileBelements.size();

        if (same) {
            for (int i = 0; same && i < fileAelements.size(); i++) {
                CodeElement<?> fileAelement = fileAelements.get(i);
                CodeElement<?> fileBelement = fileBelements.get(i);
                same = isStructureSame(fileAelement, fileBelement, relevantElementsInFileA, relevantElementsInFileB);
            }
        }

        return same;
    }

    /**
     * Checks for changes within the structure of both elements and their respective
     * nested elements.
     *
     * @param fileAElement            the file A element
     * @param fileBElement            the file B element
     * @param relevantElementsInFileA the relevancy A
     * @param relevantElementsInFileB the relevancy B
     * @return true, if unchanged
     */
    protected boolean isStructureSame(CodeElement<?> fileAElement, CodeElement<?> fileBElement,
            Set<CodeElement<?>> relevantElementsInFileA, Set<CodeElement<?>> relevantElementsInFileB) {

        boolean same = fileAElement.getPresenceCondition().equals(fileBElement.getPresenceCondition());
        if (same) {

            List<CodeElement<?>> fileAelements =
                    getListOfRelevantNestedElements((Iterable<CodeElement<?>>) fileAElement, relevantElementsInFileA);
            List<CodeElement<?>> fileBelements =
                    getListOfRelevantNestedElements((Iterable<CodeElement<?>>) fileBElement, relevantElementsInFileB);
            same = fileAelements.size() == fileBelements.size();
            if (same) {
                for (int i = 0; same && i < fileAelements.size(); i++) {
                    CodeElement<?> fileAelement = fileAelements.get(i);
                    CodeElement<?> fileBelement = fileBelements.get(i);
                    same = fileAelement.getPresenceCondition().equals(fileBelement.getPresenceCondition())
                            && isStructureSame(fileAelement, fileBelement, relevantElementsInFileA,
                                    relevantElementsInFileB);
                }
            }
        }

        return same;
    }

    /**
     * Gets the relevant blocks.
     *
     * @param parentOfNestedElements the elements
     * @param relevantElementsInFile the relevancy
     * @return the relevant blocks
     */
    protected List<CodeElement<?>> getListOfRelevantNestedElements(Iterable<CodeElement<?>> parentOfNestedElements,
            Set<CodeElement<?>> relevantElementsInFile) {
        List<CodeElement<?>> reduced = new LinkedList<CodeElement<?>>();
        for (CodeElement<?> element : parentOfNestedElements) {
            if (relevantElementsInFile == null || relevantElementsInFile.contains(element)) {
                reduced.add(element);
            }
        }
        return reduced;
    }

}
