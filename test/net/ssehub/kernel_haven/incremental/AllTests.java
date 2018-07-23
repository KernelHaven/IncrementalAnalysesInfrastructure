package net.ssehub.kernel_haven.incremental;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.incremental.diff.DiffApplyUtilTest;
import net.ssehub.kernel_haven.incremental.diff.DiffFileTest;
import net.ssehub.kernel_haven.incremental.diff.analyzer.VariabilityDiffAnalyzerTest;
import net.ssehub.kernel_haven.incremental.diff.linecount.LineCounterTest;
import net.ssehub.kernel_haven.incremental.diff.linecount.LineInfoExtractorTest;
import net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparationTest;
import net.ssehub.kernel_haven.incremental.preparation.filter.ChangeFilterTest;
import net.ssehub.kernel_haven.incremental.preparation.filter.VariabilityChangeFilterTest;
import net.ssehub.kernel_haven.incremental.storage.HybridCacheTest;

/**
 * The Class AllTests. Contains all unit-tests of for incremental analyses.
 * 
 * @author moritz
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ ChangeFilterTest.class, DiffFileTest.class,
    IncrementalPreparationTest.class, DiffApplyUtilTest.class,
    VariabilityDiffAnalyzerTest.class, VariabilityChangeFilterTest.class,
    HybridCacheTest.class, LineCounterTest.class, LineInfoExtractorTest.class})
public class AllTests {

}