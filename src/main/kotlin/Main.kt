import analyzer.AnalysisContext
import analyzer.ContextBuilder
import analyzer.Pandalyzer
import python.PythonTree
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit =
    with(ProgramArguments.parse(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                Pandalyzer.analyze(tree.root, ContextBuilder.buildWithBuiltins())
            }.let { result ->
                println(result.summarize())
                exitProcess(result.getExitValue())
            }
    }

fun AnalysisContext.getExitValue(): Int = when(this) {
    is AnalysisContext.OK -> 0
    is AnalysisContext.Error -> 1
    is AnalysisContext.Returned -> -1
}
