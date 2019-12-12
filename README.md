# IncrementalAnalysesInfrastructure

<!-- ![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=TODO) -->

Support for incremental anlyses in [KernelHaven](https://github.com/KernelHaven/KernelHaven). The initial version of this plugin was created in a master thesis and is described there ([moritzfl/sse-master-thesis](https://github.com/moritzfl/sse-master-thesis/releases)).

Since the initial version, we modified the analysis further so that we recommend users to directly refer to the documentation on github as the first primary of information.

If you are interested in evaluation results concerning this plugin, check out [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation). The release section contains the actual results of analysis executions on our reference system. 

## Usage

For the incremental infrastructure, you need a couple of settings:
- ```analysis.class```: This has to be an analysis that was explicitly developped for the incremental infrastructure. You may use a pipeline analysis but you absolutely have to make sure, that it wraps the IncrementalPostExtraction as the first executed class as this manages and hands down the HybridCache to the core analysis itself. As an example, you can look at the [IncrementalThreadedDeadCodeAnalysis class](https://github.com/KernelHaven/IncrementalDeadCodeAnalysis/blob/master/src/net/ssehub/kernel_haven/incremental/analysis/IncrementalThreadedDeadCodeAnalysis.java)
-```incremental.hybrid_cache.dir```: directory where we store files from the HybridCache (the concept of the HybridCache is explained in the wiki.
- ```incremental.input.source_tree_diff```: file path to a git-diff file that describes all changes that occured between the set of files that is currently in the folder defined by the ```source_tree``` parameter of KernelHaven. This git diff file must be generated using the command ```git diff --no-renames --binary -U100000 oldCommitHash newCommitHash```. For the first commit that you analyze in an incremental setting, the diff file must describe a commit to an empty repository and the ```source_tree``` directory must be empty.
- ```preparation.class.0 = net.ssehub.kernel_haven.incremental.preparation.IncrementalPreparation```: This is a mandatory preparation task that absolutely needs to be included in an incremental execution of KernelHaven. Any incremental analysis will not run correctly, if this setting is missing.

## Advanced Configuration


## License

This plugin is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
