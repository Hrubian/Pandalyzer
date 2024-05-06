import analyzer.AnalysisContext
import analyzer.Pandalyzer
import python.PythonTree

fun main(args: Array<String>): Unit =
    with(ProgramArguments.parse(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                AnalysisContext.buildWithBuiltins(metadata).also {
                    Pandalyzer.analyze(tree.root, it)
                }
            }.let { result ->
                when (outputFormat) {
                    OutputFormat.HumanReadable -> println(result.summarize())
                    OutputFormat.JSON -> println(result.toJson())
                }
            }
    }
