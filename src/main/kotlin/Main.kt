import analyzer.AnalysisContext
import analyzer.Pandalyzer
import python.PythonTree

fun main(args: Array<String>): Unit =
    with(ProgramArguments.parse(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                AnalysisContext.buildWithBuiltins().also {
                    Pandalyzer.analyze(tree.root, it)
                }
            }.let { result ->
                println(result.summarize())
//                exitProcess(result.getExitValue())
            }
    }

// fun AnalysisContext.getExitValue(): Int = when(this) {
//    is AnalysisContext.OK -> 0
//    is AnalysisContext.Error -> 1
//    is AnalysisContext.Returned -> -1
// }
