import net.ssehub.kernel_haven.incremental.util.diff.DiffFile;
 * information. {@link VariabilityDiffAnalyzer} will also analyze for
 * 
 * @author moritz
 * 
public class SimpleDiffAnalyzer implements DiffAnalyzer {
	public static DiffFile generateDiffFile(File file) throws IOException {
		return new DiffFile(changed);