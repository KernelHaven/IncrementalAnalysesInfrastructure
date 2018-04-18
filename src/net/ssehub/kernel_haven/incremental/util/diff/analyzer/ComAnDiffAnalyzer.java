
/* this class is a modification of diff.DiffAnalyzer-Implementation
 * from the ConAn project to suit the requirements of this project. */

 * A {@link DiffAnalyzer}-Implementation that analyzes for 
 * variability-changes as well as the type of modification 
 * (Addition / Deletion / Modification of a file).
 * Use {@link SimpleDiffAnalyzer} if you do not need information
 * about variability change as the performance penalty is lower.
 *  
 * @author Christian Kroeher, Moritz Floeter
	/** The logger. */
	
	/* (non-Javadoc)
	 * @see net.ssehub.kernel_haven.incremental.util.diff.analyzer.DiffAnalyzer#parse()
					// if the file does contain content representing the variability, build or code-model
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
		// and iterate over it as this fails with a for (int i = ...) loop
			// loop over lines