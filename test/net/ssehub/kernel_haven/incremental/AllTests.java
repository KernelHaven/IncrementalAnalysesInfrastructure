package net.ssehub.kernel_haven.incremental;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparationTest;
import net.ssehub.kernel_haven.incremental.preparation.filter.ChangedOnlyFilterTest;
import net.ssehub.kernel_haven.incremental.preparation.filter.VariabilityChangesFilterTest;
import net.ssehub.kernel_haven.incremental.util.diff.DiffFileTest;
import net.ssehub.kernel_haven.incremental.util.diff.DiffApplyUtilTest;
import net.ssehub.kernel_haven.incremental.util.diff.analyzer.VariabilityDiffAnalyzerTest;

/**
 * The Class AllTests. Contains all unit-tests of for incremental analyses.
 * 
 * @author moritz
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ ChangedOnlyFilterTest.class, DiffFileTest.class, IncrementalPreparationTest.class,
		DiffApplyUtilTest.class, VariabilityDiffAnalyzerTest.class, VariabilityChangesFilterTest.class })
public class AllTests {

}