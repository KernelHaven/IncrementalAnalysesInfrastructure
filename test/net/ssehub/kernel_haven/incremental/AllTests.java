package net.ssehub.kernel_haven.incremental;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kernel_haven.incremental.preparation.ChangedOnlyFilterTest;
import net.ssehub.kernel_haven.incremental.preparation.DiffFileTest;
import net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparationTest;
import net.ssehub.kernel_haven.incremental.preparation.VariabilityChangesFilterTest;
import net.ssehub.kernel_haven.incremental.util.ComAnDiffAnalyzerTest;
import net.ssehub.kernel_haven.incremental.util.DiffIntegrationUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
ChangedOnlyFilterTest.class,
DiffFileTest.class,
IncrementalPreparationTest.class,
DiffIntegrationUtilTest.class,
ComAnDiffAnalyzerTest.class,
VariabilityChangesFilterTest.class
})
public class AllTests {

}