package net.ssehub.kernel_haven.incremental;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.incremental.diff.analyzer.ComAnAnalyzerTest;
import net.ssehub.kernel_haven.incremental.diff.applier.FileReplacingDiffApplierTest;
import net.ssehub.kernel_haven.incremental.diff.applier.GitDiffApplierTest;
import net.ssehub.kernel_haven.incremental.diff.linecount.LineCounterTest;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileParserTest;
import net.ssehub.kernel_haven.incremental.diff.parser.DiffFileTest;
import net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparationTest;
import net.ssehub.kernel_haven.incremental.preparation.filter.ChangeFilterTest;
import net.ssehub.kernel_haven.incremental.preparation.filter.VariabilityChangeFilterTest;
import net.ssehub.kernel_haven.incremental.storage.HybridCacheTest;
import net.ssehub.kernel_haven.incremental.util.SourceFileDifferenceDetectorTest;

/**
 * The Class AllTests. Contains all unit-tests of for incremental analyses.
 * 
 * @author moritz
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ ChangeFilterTest.class, DiffFileTest.class, IncrementalPreparationTest.class, GitDiffApplierTest.class,
        FileReplacingDiffApplierTest.class, ComAnAnalyzerTest.class, VariabilityChangeFilterTest.class,
        HybridCacheTest.class, LineCounterTest.class, DiffFileParserTest.class,
        SourceFileDifferenceDetectorTest.class })
public class AllTests {

}