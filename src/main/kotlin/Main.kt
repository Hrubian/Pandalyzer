import analyzer.AnalysisContext
import analyzer.Pandalyzer
import python.PythonTree


fun main(args: Array<String>) =
    with(parseArgs(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                Pandalyzer().analyze(tree.root, AnalysisContext.empty)
            }.let { result ->
                println(result)
            }
    }