import analyzer.ContextBuilder
import analyzer.Pandalyzer
import python.PythonTree

fun main(args: Array<String>) =
    with(parseArgs(args)) {
        PythonTree.fromFile(inputFile)
            .let { tree ->
                Pandalyzer().analyze(tree.root, ContextBuilder.buildEmpty())
            }.let { result ->
                println(result)
            }
    }
