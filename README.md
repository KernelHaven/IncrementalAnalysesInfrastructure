# IncrementalAnalysesInfrastructure

<!-- ![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=TODO) -->

Support for incremental anlyses in [KernelHaven](https://github.com/KernelHaven/KernelHaven). The initial version of this plugin was created in a master thesis and is described there ([moritzfl/sse-master-thesis](https://github.com/moritzfl/sse-master-thesis/releases)).

Since the initial version, we modified the analysis further so that we recommend users to directly refer to the documentation on github as the primary source of information.

If you are interested in evaluation results concerning this plugin, check out [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation). The release section contains the actual results of analysis executions on our reference system. 

## Usage

In order to better understand how our infrastructure works, we highly recommend reading this short [overview](https://github.com/KernelHaven/IncrementalAnalysesInfrastructure/wiki/Overview) in our wiki.

For the incremental infrastructure, you need a couple of settings:

- ```incremental.hybrid_cache.dir```: directory where we store files from the HybridCache
- ```analysis.class```: This has to be an analysis that was explicitly developed for the incremental infrastructure. You may use a pipeline analysis but you absolutely have to make sure that it wraps the IncrementalPostExtraction as the first executed class as the IncrementalPostExtraction manages and hands down the HybridCache to the core analysis itself. As an example, you can look at the [IncrementalThreadedDeadCodeAnalysis class](https://github.com/KernelHaven/IncrementalDeadCodeAnalysis/blob/master/src/net/ssehub/kernel_haven/incremental/analysis/IncrementalThreadedDeadCodeAnalysis.java)
- ```incremental.input.source_tree_diff```: file path to a git-diff file that describes all changes that occured between the set of files that is currently in the folder defined by the ```source_tree``` parameter of KernelHaven and the revision that you want to analyze. This git diff file must be generated using the command ```git diff --no-renames --binary -U100000 oldCommitHash newCommitHash```. For the first commit that you analyze in an incremental setting, the diff file must describe a commit to an empty repository and the ```source_tree``` directory must be empty.
- ```preparation.class.0 = net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparation```: This is a mandatory preparation task that absolutely needs to be included in an incremental execution of KernelHaven. Any incremental analysis will not run correctly, if this setting is missing.

## Advanced Configuration
- ```incremental.variability_change_analyzer.execute```: Can be set to true or false (false by default). This defines whether changes are inspected upon their effect on variability information. If changes are not inspected for variability information, the information is also not available to filters that might require them.
- ```incremental.variability_change_analyzer.class```: This defines which class performs the inspection of variability changes. By default, we use ```net.ssehub.kernel_haven.incremental.diff.analyzer.ComAnAnalyzer``` which is based on [ComAn](https://github.com/CommitAnalysisInfrastructure/ComAnI) and specifically targets variability changes of artifacts of the Linux kernel.
- ```incremental.code.filter```, ```incremental.build.filter``` and ```incremental.variability.filter```: Defines which filters work to reduce the input for KernelHaven's extractors by identifying files that were affected by relevant changes. Our ```net.ssehub.kernel_haven.incremental.preparation.filter.VariabilityChangeFilter``` considers changes to variability for filtering while ```net.ssehub.kernel_haven.incremental.preparation.filter.ChangeFilter``` considers any artefact change to be a relevant change for the analysis. By default, the ChangeFilter is used.
- ```incremental.lines.update_lines```: Can be set to true or false (false by default). The incremental infrastructure is able to update the linenumber of files within the code model without renewed extraction of the model. This is for example useful when using a filter (such as the VariabilityChangeFilter) that may not identify every modification to a code file as relevant for the analysis. In such cases, the model for the corresponding code file is not extracted but instead we count the number of inserted and removed lines to define whether the end and start of the code blocks within the files have changed. In the event of change, we update the models accordingly.


## License

This plugin is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
