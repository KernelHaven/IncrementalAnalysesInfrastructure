/**
 * A simple {@link DiffAnalyzer}-Implementation that only analyzes
 * the type of change (that is Addition, Deletion or Modification) for
 * each file. Use {@link SimpleDiffAnalyzer} if you only need this
 * information. {@link ComAnDiffAnalyzer} will also analyze for
 * variability-changes within the file-change but will take up
 * more resources than {@link SimpleDiffAnalyzer} for the task.
 */
	/** The file that is to be parsed. */
	/**
	 * Instantiates a new {@link SimpleDiffAnalyzer}.
	 * @param diffFile the file that is to be parsed. Should be in the git-diff format.
	 */
	public SimpleDiffAnalyzer(File diffFile) {
		this.file = diffFile;
	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse()
	 */